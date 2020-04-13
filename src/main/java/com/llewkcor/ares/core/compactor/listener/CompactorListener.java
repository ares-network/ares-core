package com.llewkcor.ares.core.compactor.listener;

import com.llewkcor.ares.core.compactor.CompactManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

@AllArgsConstructor
public final class CompactorListener implements Listener {
    @Getter public final CompactManager manager;

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if (manager.isCompact(event.getItemInHand())) {
            player.sendMessage(ChatColor.RED + "This block must be decompacted before it can be placed. Type /decompact while holding this item in your hand.");
            event.setCancelled(true);
        }
    }
}