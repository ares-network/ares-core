package com.llewkcor.ares.core.snitch.listener;

import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.bukkit.Blocks;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.snitch.SnitchManager;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.llewkcor.ares.core.snitch.data.SnitchEntryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public final class SnitchListener implements Listener {
    @Getter public final SnitchManager manager;

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(new BLocatable(block), manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, block, SnitchEntryType.BLOCK_BREAK))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(new BLocatable(block), manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, block, SnitchEntryType.BLOCK_PLACE))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final BLocatable location = new BLocatable(player.getLocation().getBlock());
        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(location, manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, player.getLocation().getBlock(), SnitchEntryType.LOGOUT))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(new BLocatable(player.getLocation().getBlock()), manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, player.getLocation().getBlock(), SnitchEntryType.LOGIN))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!Blocks.isInteractable(block.getType())) {
            return;
        }

        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(new BLocatable(player.getLocation().getBlock()), manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, block, SnitchEntryType.BLOCK_INTERACTION))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        if (player == null) {
            return;
        }

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final Collection<Network> networks = manager.getPlugin().getNetworkManager().getNetworksByPlayer(player);
        final Set<UUID> networkIds = Sets.newHashSet();

        for (Network network : networks) {
            networkIds.add(network.getUniqueId());
        }

        final Runnable task = () -> {
            final List<Snitch> inRadius = manager.getSnitchByRadius(new BLocatable(player.getLocation().getBlock()), manager.getPlugin().getConfigManager().getSnitchesConfig().getSearchRadius());

            if (inRadius.isEmpty()) {
                return;
            }

            new Scheduler(getManager().getPlugin()).sync(() -> inRadius.stream()
                    .filter(snitch -> !networkIds.contains(snitch.getOwnerId()))
                    .forEach(snitch -> manager.getHandler().triggerSnitch(snitch, player, player.getLocation().getBlock(), SnitchEntryType.KILL))).run();
        };

        manager.searchQueue.add(task);
    }

    @EventHandler
    public void removePlayerOnDisconnect(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        final List<Snitch> spottedBy = manager.getSnitchBySpotted(player.getUniqueId());

        if (spottedBy.isEmpty()) {
            return;
        }

        spottedBy.forEach(snitch -> snitch.getSpotted().remove(player.getUniqueId()));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void destroySnitchOnBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final Snitch snitch = manager.getSnitchByBlock(block);

        if (snitch == null) {
            return;
        }

        manager.getHandler().deleteSnitch(snitch);
    }
}