package com.llewkcor.ares.core.claim.listener;

import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.network.data.Network;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@AllArgsConstructor
public final class ClaimListener implements Listener {
    @Getter public final ClaimManager manager;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

        if (network == null) {
            return;
        }
    }
}
