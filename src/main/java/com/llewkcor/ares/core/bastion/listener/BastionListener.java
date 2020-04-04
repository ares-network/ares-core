package com.llewkcor.ares.core.bastion.listener;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.core.bastion.BastionManager;
import com.llewkcor.ares.core.bastion.data.Bastion;
import com.llewkcor.ares.core.claim.event.BlockReinforceEvent;
import com.llewkcor.ares.core.network.data.Network;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Set;

@AllArgsConstructor
public final class BastionListener implements Listener {
    @Getter public final BastionManager manager;

    @EventHandler
    public void onReinforce(BlockReinforceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Set<Bastion> bastions = manager.getBastionInRange(new BLocatable(block), 16);

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (bastions.isEmpty()) {
            return;
        }

        for (Bastion bastion : bastions) {
            if (!bastion.isMature()) {
                continue;
            }

            final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

            if (!network.isMember(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Claim not allowed near bastion at " + bastion.getLocation().toString());
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final Bastion bastion = manager.getBastionByBlock(new BLocatable(block));

        if (bastion == null) {
            return;
        }

        manager.getHandler().deleteBastion(bastion);
    }
}