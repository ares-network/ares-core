package com.playares.core.prison;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.Ares;
import com.playares.core.prison.data.PrisonPearl;
import com.playares.core.prison.data.PrisonPearlDAO;
import com.playares.core.prison.listener.PearlTrackerListener;
import com.playares.core.prison.listener.PrisonPearlListener;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PrisonPearlManager {
    @Getter public final Ares plugin;
    @Getter public final PrisonPearlHandler handler;
    @Getter public final Set<PrisonPearl> pearlRepository;
    @Getter public final Set<UUID> mutedPearlNotifications;
    @Getter public BukkitTask expireUpdater;

    public PrisonPearlManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new PrisonPearlHandler(this);
        this.pearlRepository = Sets.newConcurrentHashSet();
        this.mutedPearlNotifications = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new PearlTrackerListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PrisonPearlListener(this), plugin);

        if (getPrisonLocation() != null) {
            Logger.print("Loading or creating the Prison World");
            Bukkit.getServer().createWorld(new WorldCreator(getPrisonLocation().getWorld().getName()).environment(World.Environment.THE_END));
            Logger.print("Finished loading the Prison World");
        } else {
            Logger.error("Failed to find Prison World Location!");
        }

        this.expireUpdater = new Scheduler(plugin).async(() -> {
            final Set<PrisonPearl> expired = pearlRepository.stream().filter(prisonPearl -> prisonPearl.isExpired() && !prisonPearl.isReleased()).collect(Collectors.toSet());

            expired.forEach(expiredPearl -> {
                PrisonPearlDAO.deletePearl(plugin.getDatabaseInstance(), expiredPearl);
                new Scheduler(plugin).sync(() -> handler.releasePearl(expiredPearl, "Imprisonment has naturally expired")).run();
            });

            new Scheduler(plugin).sync(() -> {
                final Set<PrisonPearl> invalid = pearlRepository.stream().filter(prisonPearl -> prisonPearl.getBukkitLocation().getY() <= -25.0).collect(Collectors.toSet());

                new Scheduler(plugin).async(() -> invalid.forEach(invalidPearl -> {
                    PrisonPearlDAO.deletePearl(plugin.getDatabaseInstance(), invalidPearl);
                    new Scheduler(plugin).sync(() -> handler.releasePearl(invalidPearl, "Pearl fell in to the void")).run();
                })).run();
            }).run();
        }).repeat(20L, 5 * 20L).run();
    }

    /**
     * Returns a PrisonPearl instance matching the provided imprisoned Bukkit UUID
     * @param uniqueId Bukkit UUID
     * @return Prison Pearl
     */
    public PrisonPearl getPrisonPearlByPlayer(UUID uniqueId) {
        return pearlRepository.stream().filter(pearl -> pearl.getImprisonedUUID().equals(uniqueId) && !pearl.isExpired()).findFirst().orElse(null);
    }

    /**
     * Returns a PrisonPearl instance matching the provided imprisoned username
     * @param username Bukkit Username
     * @return Prison Pearl
     */
    public PrisonPearl getPrisonPearlByPlayer(String username) {
        return pearlRepository.stream().filter(pearl -> pearl.getImprisonedUsername().equalsIgnoreCase(username) && !pearl.isExpired()).findFirst().orElse(null);
    }

    /**
     * Returns a PrisonPearl instance matching the provided ItemStack
     * @param item Bukkit ItemStack
     * @return Prison Pearl
     */
    public PrisonPearl getPrisonPearlByItem(ItemStack item) {
        return pearlRepository.stream().filter(pearl -> pearl.match(item) && !pearl.isExpired()).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable List containing all active Prison Pearls
     * @return Immutable List of Prison Pearls
     */
    public ImmutableList<PrisonPearl> getActivePrisonPearls() {
        return ImmutableList.copyOf(pearlRepository.stream().filter(pearl -> !pearl.isReleased() && !pearl.isExpired()).collect(Collectors.toList()));
    }

    /**
     * Returns true if the provided Bukkit Player has prison pearl notifications muted
     * @param player Player
     * @return True if muted
     */
    public boolean isPearlNotificationsMuted(Player player) {
        return mutedPearlNotifications.contains(player.getUniqueId());
    }

    /**
     * Returns the spawn location for the Prison Pearl World
     * @return Bukkit Location
     */
    public Location getPrisonLocation() {
        final World world = Bukkit.getWorld(plugin.getConfigManager().getPrisonPearlConfig().getBanWorldName());

        if (world == null) {
            Logger.error("Attempted to load the Prison World but could not find it");
            return null;
        }

        return world.getHighestBlockAt(world.getSpawnLocation().getBlockX(), world.getSpawnLocation().getBlockZ()).getLocation();
    }

    /**
     * Returns true if the provided item was at some point a prison pearl but is now expired
     * @param item Bukkit ItemStack
     * @return True if this is an expired prison pearl item
     */
    public boolean isExpiredPrisonPearl(ItemStack item) {
        final PrisonPearl existing = getPrisonPearlByItem(item);

        if (existing != null) {
            return false;
        }

        final ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) {
            return false;
        }

        final List<String> lore = meta.getLore();

        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (String loreLine : lore) {
            if (loreLine.contains(ChatColor.DARK_PURPLE + "Prison Pearl")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the ban duration for the provided Bukkit Player
     * @param player Player
     * @return Ban duration (in seconds)
     */
    public int getBanDuration(Player player) {
        int lowest = plugin.getConfigManager().getPrisonPearlConfig().getBanDuration();

        for (String permission : plugin.getConfigManager().getPrisonPearlConfig().getPremiumBanDurations().keySet()) {
            final int duration = plugin.getConfigManager().getPrisonPearlConfig().getPremiumBanDurations().getOrDefault(permission, plugin.getConfigManager().getPrisonPearlConfig().getBanDuration());

            if (permission == null || duration <= 0) {
                continue;
            }

            if (!player.hasPermission(permission)) {
                continue;
            }

            if (duration < lowest) {
                lowest = duration;
            }
        }

        return lowest;
    }
}