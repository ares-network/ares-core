package com.llewkcor.ares.core.acid.listener;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.core.acid.AcidManager;
import com.llewkcor.ares.core.acid.data.AcidBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@AllArgsConstructor
public final class AcidListener implements Listener {
    @Getter public final AcidManager manager;

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final AcidBlock acid = manager.getAcidBlockByBlock(new BLocatable(block));

        if (acid == null) {
            return;
        }

        manager.getHandler().deleteAcid(acid);
    }
}
