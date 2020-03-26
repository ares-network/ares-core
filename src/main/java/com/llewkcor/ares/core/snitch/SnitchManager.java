package com.llewkcor.ares.core.snitch;

import com.google.common.collect.Sets;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.snitch.data.Snitch;
import lombok.Getter;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.UUID;

public final class SnitchManager {
    @Getter public Ares plugin;
    @Getter public final SnitchHandler handler;
    @Getter public final Set<Snitch> snitchRepository;

    public SnitchManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new SnitchHandler(this);
        this.snitchRepository = Sets.newConcurrentHashSet();
    }

    /**
     * Returns a Snitch instance matching the provided UUID
     * @param uniqueId Snitch ID
     * @return Snitch
     */
    public Snitch getSnitchByID(UUID uniqueId) {
        return snitchRepository.stream().filter(snitch -> snitch.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Snitch instance matching the location of the provided Bukkit Block
     * @param block Bukkit Block
     * @return Snitch
     */
    public Snitch getSnitchByBlock(Block block) {
        final double x = block.getLocation().getX();
        final double y = block.getLocation().getY();
        final double z = block.getLocation().getZ();
        final String world = block.getLocation().getWorld().getName();

        return snitchRepository.stream().filter(snitch ->
                snitch.getLocation().getX() == x && snitch.getLocation().getY() == y && snitch.getLocation().getZ() == z && snitch.getLocation().getWorldName().equals(world)).findFirst().orElse(null);
    }
}