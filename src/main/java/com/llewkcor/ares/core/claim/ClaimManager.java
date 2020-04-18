package com.llewkcor.ares.core.claim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.claim.experimental.ExperimentalLoadManager;
import com.llewkcor.ares.core.claim.listener.ClaimCreatorListener;
import com.llewkcor.ares.core.claim.listener.ClaimListener;
import com.llewkcor.ares.core.claim.session.ClaimSession;
import com.llewkcor.ares.core.network.data.Network;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimManager {
    @Getter public final Ares plugin;
    @Getter public final ClaimHandler handler;
    @Getter public final Set<Claim> claimRepository;
    @Getter public final Set<ClaimSession> activeClaimSessions;
    @Getter public final ExperimentalLoadManager experimentalLoadManager;

    public ClaimManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new ClaimHandler(this);
        this.claimRepository = Sets.newConcurrentHashSet();
        this.activeClaimSessions = Sets.newConcurrentHashSet();
        this.experimentalLoadManager = new ExperimentalLoadManager(this);

        Bukkit.getPluginManager().registerEvents(new ClaimListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ClaimCreatorListener(this), plugin);
    }

    /**
     * Returns a Claim matching the provided UUID
     * @param uniqueId Claim UUID
     * @return Claim
     */
    public Claim getClaimByID(UUID uniqueId) {
        return claimRepository.stream().filter(claim -> claim.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Claim matching the provided Bukkit Block
     * @param block Bukkit Block
     * @return Claim
     */
    public Claim getClaimByBlock(Block block) {
        return claimRepository
                .stream()
                .filter(
                        claim ->
                                claim.getLocation().getX() == block.getLocation().getX() &&
                                claim.getLocation().getY() == block.getLocation().getY() &&
                                claim.getLocation().getZ() == block.getLocation().getZ() &&
                                claim.getLocation().getWorldName().equals(block.getLocation().getWorld().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a Claim matching the provided BLocatable
     * @param block BLocatable
     * @return Claim
     */
    public Claim getClaimByBlock(BLocatable block) {
        return claimRepository
                .stream()
                .filter(
                        claim ->
                                claim.getLocation().getX() == block.getX() &&
                                claim.getLocation().getY() == block.getY() &&
                                claim.getLocation().getZ() == block.getZ() &&
                                claim.getLocation().getWorldName().equals(block.getWorldName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns an Immutable List containing all Claims inside a provided Chunk
     * @param chunkX Chunk X
     * @param chunkZ Chunk Z
     * @param worldName Chunk World Name
     * @return Immutable List of Claims
     */
    public ImmutableList<Claim> getClaimByChunk(int chunkX, int chunkZ, String worldName) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.getChunkX() == chunkX && claim.getChunkZ() == chunkZ && claim.getChunkWorld().equals(worldName)).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable List containing all Claim blocks owned by the provided Network UUID
     * @param network Network
     * @return ImmutableList of Claims
     */
    public ImmutableList<Claim> getClaimByOwner(Network network) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.getOwnerId().equals(network.getUniqueId())).collect(Collectors.toList()));
    }

    /**
     * Returns a Claim Session instance matching the provided Bukkit Player's UUID
     * @param player Bukkit Player
     * @return ClaimSession
     */
    public ClaimSession getSessionByPlayer(Player player) {
        return activeClaimSessions.stream().filter(claimSession -> claimSession.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}