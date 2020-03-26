package com.llewkcor.ares.core.snitch;

import com.llewkcor.ares.commons.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class SnitchHandler {
    /*
    createSnitch
    triggerSnitch
    breakSnitch
     */

    @Getter public final SnitchManager manager;

    public SnitchHandler(SnitchManager manager) {
        this.manager = manager;
    }

    /**
     * Load all snitches from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {

    }

    /**
     * Save all snitches in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {

    }

    /**
     * Handles the creation of a new Snitch
     * @param player Player
     * @param block Block
     * @param networkName Network Name
     * @param description Snitch Description
     * @param promise Promise
     */
    public void createSnitch(Player player, Block block, String networkName, String description, SimplePromise promise) {

    }
}