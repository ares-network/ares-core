package com.llewkcor.ares.core.claim.experimental;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.claim.data.ClaimDAO;
import com.llewkcor.ares.core.claim.experimental.data.QueuedChunk;
import com.llewkcor.ares.core.claim.experimental.data.QueuedChunkStatus;
import com.llewkcor.ares.core.claim.experimental.listener.ExperimentalLoadListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class ExperimentalLoadManager {
    @Getter public final ClaimManager manager;
    @Getter public final Set<QueuedChunk> queuedChunks;
    @Getter public final BukkitTask queueProcessor;

    public ExperimentalLoadManager(ClaimManager manager) {
        this.manager = manager;
        this.queuedChunks = Sets.newConcurrentHashSet();
        this.queueProcessor = new Scheduler(manager.getPlugin()).async(() -> {

            // Handles loading new chunks in to memory
            final Set<QueuedChunk> load = getChunks(QueuedChunkStatus.LOAD);
            load.forEach(loadEntry -> {
                loadEntry.setStatus(QueuedChunkStatus.READY);

                final Collection<Claim> toLoad = ClaimDAO.getChunkClaims(manager.getPlugin().getDatabaseInstance(), loadEntry.getX(), loadEntry.getZ(), loadEntry.getWorld());

                manager.getClaimRepository().addAll(toLoad);
            });

            // Handles unloading chunks from memory to the database
            final Set<QueuedChunk> unload = getChunks(QueuedChunkStatus.UNLOAD);
            unload.forEach(unloadEntry -> {
                final Collection<Claim> toUnload = manager.getClaimByChunk(unloadEntry.getX(), unloadEntry.getZ(), unloadEntry.getWorld());

                ClaimDAO.saveClaims(manager.getPlugin().getDatabaseInstance(), toUnload);

                manager.getClaimRepository().removeAll(toUnload);
            });

            queuedChunks.removeAll(unload);

        }).repeat(0L, 1L).run();

        Bukkit.getPluginManager().registerEvents(new ExperimentalLoadListener(this), manager.getPlugin());
    }

    public QueuedChunk getChunkAt(int x, int z, String world) {
        return queuedChunks.stream().filter(chunk -> chunk.getX() == x && chunk.getZ() == z && chunk.getWorld().equals(world)).findFirst().orElse(null);
    }

    public QueuedChunk getChunkAt(Chunk chunk) {
        return getChunkAt(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
    }

    private ImmutableSet<QueuedChunk> getChunks(QueuedChunkStatus status) {
        return ImmutableSet.copyOf(queuedChunks.stream().filter(chunk -> chunk.getStatus().equals(status)).collect(Collectors.toList()));
    }
}