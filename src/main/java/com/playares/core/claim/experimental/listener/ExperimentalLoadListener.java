package com.playares.core.claim.experimental.listener;

import com.playares.core.claim.experimental.ExperimentalLoadManager;
import com.playares.core.claim.experimental.data.QueuedChunk;
import com.playares.core.claim.experimental.data.QueuedChunkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@AllArgsConstructor
public final class ExperimentalLoadListener implements Listener {
    @Getter public final ExperimentalLoadManager manager;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        final QueuedChunk queued = manager.getChunkAt(chunk);

        if (queued == null) {
            final QueuedChunk newQueue = new QueuedChunk(QueuedChunkStatus.LOAD, chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
            manager.getQueuedChunks().add(newQueue);
            return;
        }

        if (queued.getStatus().equals(QueuedChunkStatus.UNLOAD)) {
            queued.setStatus(QueuedChunkStatus.LOAD);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Chunk chunk = event.getChunk();
        final QueuedChunk queued = manager.getChunkAt(chunk);

        if (queued == null || !queued.getStatus().equals(QueuedChunkStatus.READY)) {
            return;
        }

        queued.setStatus(QueuedChunkStatus.UNLOAD);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Chunk chunk = block.getChunk();
        final QueuedChunk queued = manager.getChunkAt(chunk);

        if (queued == null) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "This chunk has not been processed yet");
            return;
        }
    }
}