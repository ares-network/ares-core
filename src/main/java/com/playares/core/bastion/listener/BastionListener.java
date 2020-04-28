package com.playares.core.bastion.listener;

import com.playares.commons.location.BLocatable;
import com.playares.core.bastion.BastionManager;
import com.playares.core.bastion.data.Bastion;
import com.playares.core.claim.event.BlockReinforceEvent;
import com.playares.core.network.data.Network;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public final class BastionListener implements Listener {
    @Getter public final BastionManager manager;

    @EventHandler
    public void onReinforce(BlockReinforceEvent event) {
        final Player player = event.getPlayer();
        final List<Block> blocks = event.getBlocks();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        for (Block block : blocks) {
            final Set<Bastion> bastions = manager.getBastionInRange(new BLocatable(block), manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

            if (bastions.isEmpty()) {
                continue;
            }

            for (Bastion bastion : bastions) {
                if (!bastion.isMature()) {
                    continue;
                }

                final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

                if (!network.isMember(player)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can not claim blocks near bastion at " + bastion.getLocation().toString());
                    return;
                }
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

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        final List<Block> blocks = event.getBlocks();

        for (Block block : blocks) {
            final BLocatable location = new BLocatable(block);
            final Bastion bastion = manager.getBastionByBlock(location);
            final Set<Bastion> inRange = manager.getBastionInRange(location, manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

            if (bastion != null || !inRange.isEmpty()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        final List<Block> blocks = event.getBlocks();

        for (Block block : blocks) {
            final BLocatable location = new BLocatable(block);
            final Bastion bastion = manager.getBastionByBlock(location);
            final Set<Bastion> inRange = manager.getBastionInRange(location, manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

            if (bastion != null || !inRange.isEmpty()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}