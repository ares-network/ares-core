package com.llewkcor.ares.core.claim.experimental.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

@AllArgsConstructor
public final class QueuedChunk {
    @Getter @Setter public QueuedChunkStatus status;
    @Getter public final int x;
    @Getter public final int z;
    @Getter public final String world;

    public Chunk getBukkit() {
        return Bukkit.getWorld(world).getChunkAt(x, z);
    }
}