package com.llewkcor.ares.core.acid;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.acid.data.AcidBlock;
import com.llewkcor.ares.core.acid.listener.AcidListener;
import com.llewkcor.ares.core.claim.data.ClaimDAO;
import com.llewkcor.ares.core.network.data.Network;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AcidManager {
    @Getter public final Ares plugin;
    @Getter public AcidHandler handler;
    @Getter public final Set<AcidBlock> acidRepository;
    @Getter public final BukkitTask tickingTask;

    public AcidManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new AcidHandler(this);
        this.acidRepository = Sets.newConcurrentHashSet();
        this.tickingTask = new Scheduler(plugin).async(() -> {

            // Gets all Acid Blocks that are matured & not expired
            acidRepository.stream().filter(acid -> acid.isMature() && !acid.isExpired()).forEach(acidBlock ->
                    // Gets all Claim Blocks that are within radius that are matured
                    plugin.getClaimManager().getClaimRepository().stream().filter(claim -> !claim.getOwnerId().equals(acidBlock.getOwnerId()) && claim.isMatured() && acidBlock.inside(claim.getLocation(), plugin.getConfigManager().getAcidConfig().getAcidRadius())).forEach(nearbyClaim -> {

                        nearbyClaim.setHealth(nearbyClaim.getHealth() - 1);

                        // We killed him
                        if (nearbyClaim.getHealth() <= 0) {
                            plugin.getClaimManager().getClaimRepository().remove(nearbyClaim);
                            ClaimDAO.deleteClaim(plugin.getDatabaseInstance(), nearbyClaim);

                            new Scheduler(plugin).sync(() -> {
                                final Network acidBlockOwner = plugin.getNetworkManager().getNetworkByID(acidBlock.getOwnerId());

                                if (acidBlockOwner != null) {
                                    acidBlockOwner.sendMessage(ChatColor.YELLOW + acidBlockOwner.getName() + "'s Acid Block destroyed a Claim at " + nearbyClaim.getLocation().toString());
                                }

                                Logger.print("Acid Block at " + acidBlock.getLocation().toString() + " destroyed claim at " + nearbyClaim.getLocation().toString());
                            }).run();
                        }

            }));

        }).repeat(0L, plugin.getConfigManager().getAcidConfig().getAcidTickInterval() * 20L).run();

        Bukkit.getPluginManager().registerEvents(new AcidListener(this), plugin);
    }

    /**
     * Returns an Acid Block matching the provided UUID
     * @param uniqueId UUID
     * @return Acid Block
     */
    public AcidBlock getAcidBlockByID(UUID uniqueId) {
        return acidRepository.stream().filter(acid -> acid.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns an Acid Block matching the provided Block location
     * @param location Location
     * @return Acid Block
     */
    public AcidBlock getAcidBlockByBlock(BLocatable location) {
        return acidRepository.stream().filter(acid ->

                        acid.getLocation().getX() == location.getX() &&
                        acid.getLocation().getY() == location.getY() &&
                        acid.getLocation().getZ() == location.getZ() &&
                        acid.getLocation().getWorldName().equals(location.getWorldName()))

                .findFirst()
                .orElse(null);
    }

    /**
     * Returns an Immutable Set of Acid Blocks matching the provided Network
     * @param network Network
     * @return Immutable Set of Acid Blocks
     */
    public ImmutableSet<AcidBlock> getAcidBlockByOwner(Network network) {
        return ImmutableSet.copyOf(acidRepository.stream().filter(acid -> acid.getOwnerId().equals(network.getUniqueId())).collect(Collectors.toSet()));
    }

    /**
     * Returns an Immutable Set of Acid Blocks that are expired
     * @return Immutable Set of Acid Blocks
     */
    public ImmutableSet<AcidBlock> getExpiredAcidBlocks() {
        return ImmutableSet.copyOf(acidRepository.stream().filter(acid -> acid.isExpired()).collect(Collectors.toSet()));
    }
}