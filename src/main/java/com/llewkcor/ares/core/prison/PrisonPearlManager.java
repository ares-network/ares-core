package com.llewkcor.ares.core.prison;

import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.prison.listener.PearlTrackerListener;
import com.llewkcor.ares.core.prison.listener.PrisonPearlListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public final class PrisonPearlManager {
    @Getter public final Ares plugin;
    @Getter public final PrisonPearlHandler handler;
    @Getter public final Set<PrisonPearl> pearlRepository;

    public PrisonPearlManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new PrisonPearlHandler(this);
        this.pearlRepository = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new PearlTrackerListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PrisonPearlListener(this), plugin);
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
}