package com.llewkcor.ares.core.snitch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.llewkcor.ares.core.snitch.data.SnitchEntryType;
import com.llewkcor.ares.core.snitch.listener.SnitchListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SnitchManager {
    @Getter public Ares plugin;
    @Getter public final SnitchHandler handler;
    @Getter public final Set<Snitch> snitchRepository;
    @Getter public final List<Runnable> searchQueue;
    @Getter public BukkitTask queueTask;
    @Getter public BukkitTask movementTask;

    public SnitchManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new SnitchHandler(this);
        this.snitchRepository = Sets.newConcurrentHashSet();
        this.searchQueue = Lists.newArrayList();

        Bukkit.getPluginManager().registerEvents(new SnitchListener(this), plugin);

        this.queueTask = new Scheduler(plugin).async(() -> {
            final List<Runnable> snapshot = Lists.newArrayList(searchQueue);

            for (Runnable runnable : snapshot) {
                if (runnable == null) {
                    continue;
                }

                new Scheduler(plugin).async(runnable).run();
            }

            searchQueue.clear();
        }).repeat(0L, 1L).run();

        this.movementTask = new Scheduler(plugin).sync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                final UUID bukkitUUID = player.getUniqueId();
                final BLocatable currentLocation = new BLocatable(player.getLocation().getBlock());
                final Collection<Network> networks = plugin.getNetworkManager().getNetworksByPlayer(player);
                final Set<UUID> networkIds = Sets.newHashSet();
                final boolean admin = player.hasPermission("arescore.admin");

                if (admin) {
                    continue;
                }

                for (Network network : networks) {
                    networkIds.add(network.getUniqueId());
                }

                final Runnable task = () -> {
                    final List<Snitch> spottedSnitches = getSnitchBySpotted(bukkitUUID);
                    final List<Snitch> radiusSnitches = getSnitchByRadius(currentLocation, getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());
                    final List<Snitch> toRemove = Lists.newArrayList();
                    final List<Snitch> toAdd = Lists.newArrayList();

                    // Removes all snitches spotted by but are no longer in radius of
                    toRemove.addAll(spottedSnitches.stream()
                            .filter(snitch -> !radiusSnitches.contains(snitch))
                            .collect(Collectors.toList()));

                    // Adds all snitches in radius that are not marked as spotted
                    toAdd.addAll(radiusSnitches.stream()
                            .filter(snitch -> !spottedSnitches.contains(snitch))
                            .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                            .collect(Collectors.toList()));

                    // Updates the location for all snitches already spotted by
                    spottedSnitches
                            .stream()
                            .filter(snitch -> snitch.inRadius(currentLocation, getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius()))
                            .forEach(snitch -> snitch.getSpotted().put(bukkitUUID, currentLocation));

                    // Remove spotted status from removed snitches
                    toRemove.forEach(removed -> removed.getSpotted().remove(bukkitUUID));

                    toAdd.forEach(added -> {
                        // Setting current location for snitch last seen check
                        added.getSpotted().put(bukkitUUID, currentLocation);

                        // Triggering snitch for newly added snitches
                        new Scheduler(plugin).sync(() -> handler.triggerSnitch(added, player, currentLocation, Material.AIR, SnitchEntryType.SPOTTED)).run();
                    });
                };

                searchQueue.add(task);
            }
        }).repeat(0L, 3 * 20L).run();
    }

    /**
     * Returns a Snitch instance matching the provided UUID
     * @param uniqueId Snitch ID
     * @return Snitch
     */
    public Snitch getSnitchByID(UUID uniqueId) {
        return snitchRepository.stream().filter(snitch -> snitch.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Snitch instance matching the location of the provided Bukkit Block
     * @param block Bukkit Block
     * @return Snitch
     */
    public Snitch getSnitchByBlock(Block block) {
        final double x = block.getLocation().getX();
        final double y = block.getLocation().getY();
        final double z = block.getLocation().getZ();
        final String world = block.getLocation().getWorld().getName();

        return snitchRepository.stream().filter(snitch ->
                snitch.getLocation().getX() == x && snitch.getLocation().getY() == y && snitch.getLocation().getZ() == z && snitch.getLocation().getWorldName().equals(world)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable List of Snitch instances within the provided radius of the provided block location
     * @param location Block location
     * @param radius Radius
     * @return Immutable List of Snitches
     */
    public ImmutableList<Snitch> getSnitchByRadius(BLocatable location, double radius) {
        return ImmutableList.copyOf(snitchRepository.stream().filter(snitch -> snitch.inRadius(location, radius) && snitch.isMature()).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable List of Snitch instances the provided Bukkit UUID is currently spotted by
     * @param uniqueId Bukkit UUID
     * @return Immutable List of Snitches
     */
    public ImmutableList<Snitch> getSnitchBySpotted(UUID uniqueId) {
        return ImmutableList.copyOf(snitchRepository.stream().filter(snitch -> snitch.getSpotted().containsKey(uniqueId)).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable List of Snitch instances that belong to the provided Network
     * @param network Network
     * @return Immutable List of Snitches
     */
    public ImmutableList<Snitch> getSnitchByOwner(Network network) {
        return ImmutableList.copyOf(snitchRepository.stream().filter(snitch -> snitch.getOwnerId().equals(network.getUniqueId())).collect(Collectors.toList()));
    }
}