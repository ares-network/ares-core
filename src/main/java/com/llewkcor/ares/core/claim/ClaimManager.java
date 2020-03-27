package com.llewkcor.ares.core.claim;

import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.claim.data.Claim;
import lombok.Getter;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.UUID;

public final class ClaimManager {
    @Getter public final Ares plugin;
    @Getter public final Set<Claim> claimRepository;

    public ClaimManager(Ares plugin) {
        this.plugin = plugin;
        this.claimRepository = Sets.newConcurrentHashSet();
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
}