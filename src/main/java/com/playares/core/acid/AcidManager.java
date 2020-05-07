package com.playares.core.acid;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.commons.location.BLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.Ares;
import com.playares.core.acid.data.AcidBlock;
import com.playares.core.acid.listener.AcidListener;
import com.playares.core.bastion.data.Bastion;
import com.playares.core.claim.data.ClaimDAO;
import com.playares.core.network.data.Network;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
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
                        // Gets all nearby bastions this block is in range of
                        final ImmutableSet<Bastion> nearbyBastions = plugin.getBastionManager().getBastionInRange(nearbyClaim.getLocation(), plugin.getConfigManager().getBastionsConfig().getBastionRadius());
                        final List<Bastion> badBastions = nearbyBastions.stream().filter(bastion -> bastion.isMature() && !bastion.getOwnerId().equals(acidBlock.getOwnerId())).collect(Collectors.toList());

                        // This code performs if there aren't any nearby bastions that aren't owned by the Acid blocking network
                        if (badBastions.isEmpty()) {
                            nearbyClaim.setHealth(nearbyClaim.getHealth() - 1);
                            acidBlock.addDamage(1);

                            // We killed him
                            if (nearbyClaim.getHealth() <= 0) {
                                plugin.getClaimManager().getClaimRepository().remove(nearbyClaim);
                                ClaimDAO.deleteClaim(plugin.getDatabaseInstance(), nearbyClaim);

                                new Scheduler(plugin).sync(() -> {
                                    final Network acidBlockOwner = plugin.getNetworkManager().getNetworkByID(acidBlock.getOwnerId());

                                    if (acidBlockOwner != null) {
                                        acidBlockOwner.sendMessage(ChatColor.AQUA + "Acid Block" + ChatColor.YELLOW + " destroyed a claim at " + ChatColor.BLUE + nearbyClaim.getLocation().toString());
                                    }

                                    Logger.print("Acid Block at " + acidBlock.getLocation().toString() + " destroyed claim at " + nearbyClaim.getLocation().toString());
                                }).run();
                            }
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
        return ImmutableSet.copyOf(acidRepository.stream().filter(AcidBlock::isExpired).collect(Collectors.toSet()));
    }

    /**
     * Returns an Immutable Set containing Acid Blocks inside the provided Chunk
     * @param chunk Chunk
     * @return Immutable Set of Acid Blocks
     */
    public ImmutableSet<AcidBlock> getAcidBlockByChunk(Chunk chunk) {
        return ImmutableSet.copyOf(acidRepository.stream().filter(acid -> acid.getChunkX() == chunk.getX() && acid.getChunkZ() == chunk.getZ() && acid.getLocation().getWorldName().equals(chunk.getWorld().getName())).collect(Collectors.toSet()));
    }
}