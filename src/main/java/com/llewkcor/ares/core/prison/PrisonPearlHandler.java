package com.llewkcor.ares.core.prison;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.prison.data.PearlLocationType;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.prison.data.PrisonPearlDAO;
import com.llewkcor.ares.core.prison.event.PrisonPearlCreateEvent;
import com.llewkcor.ares.core.prison.event.PrisonPearlReleaseEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PrisonPearlHandler {
    @Getter public final PrisonPearlManager manager;

    public PrisonPearlHandler(PrisonPearlManager manager) {
        this.manager = manager;
    }

    /**
     * Load all snitches from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all prison pearls from the database");
            manager.getPearlRepository().addAll(PrisonPearlDAO.getPearls(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getPearlRepository().size() + " Prison Pearls");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getPearlRepository().addAll(PrisonPearlDAO.getPearls(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getPearlRepository().size() + " Prison Pearls")).run();
        }).run();
    }

    /**
     * Save all snitches in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all prison pearls to the database");
            PrisonPearlDAO.savePearls(manager.getPlugin().getDatabaseInstance(), manager.getPearlRepository());
            Logger.print("Saved " + manager.getPearlRepository().size() + " Prison Pearls");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            PrisonPearlDAO.savePearls(manager.getPlugin().getDatabaseInstance(), manager.getPearlRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getPearlRepository().size() + " Prison Pearls")).run();
        }).run();
    }

    /**
     * Handles releasing a player from imprisonment
     * @param pearl Prison Pearl
     * @param reason Release Reason
     */
    public void releasePearl(PrisonPearl pearl, String reason) {
        if (pearl.isExpired()) {
            return;
        }

        final Player freePlayer = pearl.getImprisoned();

        if (freePlayer != null) {
            freePlayer.sendMessage(ChatColor.GREEN + "You have been set free! Reason: " + reason);
        }

        pearl.setExpireTime(Time.now());

        final PrisonPearlReleaseEvent releaseEvent = new PrisonPearlReleaseEvent(pearl, reason);
        Bukkit.getPluginManager().callEvent(releaseEvent);

        new Scheduler(manager.getPlugin()).async(() -> PrisonPearlDAO.deletePearl(manager.getPlugin().getDatabaseInstance(), pearl)).run();
    }

    /**
     * Handles imprisoning a player
     * @param imprisonedUsername Imprisoned Player Username
     * @param imprisonedUUID Imprisoned Player UUID
     * @param killer Bukkit Player Killer
     */
    public void imprisonPlayer(String imprisonedUsername, UUID imprisonedUUID, Player killer) {
        final PrisonPearl pearl;

        if (killer.getInventory().firstEmpty() == -1) {
            pearl = new PrisonPearl(imprisonedUsername, imprisonedUUID, killer.getName(), killer.getUniqueId(), (Time.now() + (manager.getPlugin().getConfigManager().getPrisonPearlConfig().getBanDuration() * 1000L)), new BLocatable(killer.getLocation().getBlock()), PearlLocationType.GROUND);

            final Item item = killer.getWorld().dropItemNaturally(killer.getLocation(), pearl.getItem());

            pearl.setTrackedItem(item);
            pearl.setLocation(new BLocatable(item.getLocation().getBlock()));

            killer.sendMessage(ChatColor.RED + imprisonedUsername + "'s Prison Pearl has dropped at your feet because your inventory is full");
        } else {
            pearl = new PrisonPearl(imprisonedUsername, imprisonedUUID, killer.getName(), killer.getUniqueId(), (Time.now() + (manager.getPlugin().getConfigManager().getPrisonPearlConfig().getBanDuration() * 1000L)), new BLocatable(killer.getLocation().getBlock()), PearlLocationType.PLAYER);

            killer.getInventory().addItem(pearl.getItem());
            killer.updateInventory();

            killer.sendMessage(ChatColor.GREEN + imprisonedUsername + "'s Prison Pearl has been placed in your inventory");
        }

        manager.getPearlRepository().add(pearl);

        final PrisonPearlCreateEvent createEvent = new PrisonPearlCreateEvent(pearl);
        Bukkit.getPluginManager().callEvent(createEvent);
    }

    /**
     * Handles providing the location of a Prison Pearl
     * @param player Player
     * @param username Searching Username (optional)
     * @param promise Promise
     */
    public void locatePearl(Player player, String username, FailablePromise<String> promise) {
        final PrisonPearl pearl;

        if (username != null) {
            pearl = manager.getPrisonPearlByPlayer(username);
        } else {
            pearl = manager.getPrisonPearlByPlayer(player.getUniqueId());
        }

        if (pearl == null || pearl.isExpired()) {
            promise.fail("Player is not imprisoned");
            return;
        }

        if (pearl.getLocationType().equals(PearlLocationType.PLAYER)) {
            final Player locatePlayer = pearl.getKiller();

            if (locatePlayer != null) {
                pearl.setLocation(new BLocatable(locatePlayer.getLocation().getBlock()));
            }
        }

        final Location location = pearl.getBukkitLocation();
        final PearlLocationType locationType = pearl.getLocationType();

        if (locationType.equals(PearlLocationType.PLAYER)) {
            promise.success(ChatColor.GRAY + "Your pearl is being held by a player at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
        } else if (locationType.equals(PearlLocationType.CONTAINER)) {
            promise.success(ChatColor.GRAY + "Your pearl is being held in a container at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
        } else {
            promise.success(ChatColor.GRAY + "Your pearl is laying on the ground at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
        }
    }
}