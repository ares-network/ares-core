package com.llewkcor.ares.core.factory.listener;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.factory.FactoryManager;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryDAO;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@AllArgsConstructor
public final class FactoryListener implements Listener {
    @Getter public final FactoryManager manager;

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final Factory factory = manager.getFactoryByBlock(new BLocatable(block));

        if (factory == null) {
            return;
        }

        manager.getFactoryRepository().remove(factory);

        new Scheduler(manager.getPlugin()).async(() -> FactoryDAO.deleteFactory(manager.getPlugin().getDatabaseInstance(), factory)).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        final Factory factory = manager.getFactoryByBlock(new BLocatable(block));

        if (factory == null) {
            return;
        }

        final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(factory.getOwnerId());

        if (owner == null) {
            Logger.error("Failed to obtain factory owner information for Factory (" + factory.getUniqueId().toString() + ")");

            manager.getFactoryRepository().remove(factory);

            new Scheduler(manager.getPlugin()).async(() -> FactoryDAO.deleteFactory(manager.getPlugin().getDatabaseInstance(), factory)).run();

            return;
        }

        if (block.getType().equals(Material.FURNACE)) {
            event.setCancelled(true);
            manager.getMenuHandler().openFactoryJobs(player, factory);

            return;
        }

        if (block.getType().equals(Material.WORKBENCH)) {
            event.setCancelled(true);
            manager.getMenuHandler().openFactoryRecipes(player, factory);
        }
    }
}