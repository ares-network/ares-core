package com.llewkcor.ares.core.utils;

import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;

import java.util.List;

public final class BlockUtil {
    /**
     * Returns an array of block locations for multi-block placements
     * @param block Origin Block
     * @return Array of Blocks
     */
    public static List<Block> getMultiblockLocations(Block block) {
        final List<Block> blocks = Lists.newArrayList();

        blocks.add(block);

        if (block == null || block.getType().equals(Material.AIR)) {
            return blocks;
        }

        if (block.getType().name().contains("_DOOR") && !block.getType().equals(Material.TRAP_DOOR)) {
            final Door door = (Door)block.getState().getData();

            if (door.isTopHalf()) {
                blocks.add(block.getRelative(BlockFace.DOWN));
            } else {
                blocks.add(block.getRelative(BlockFace.UP));
            }

            return blocks;
        }

        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            final Chest chest = (Chest)block.getState();

            if (!(chest.getInventory() instanceof DoubleChestInventory)) {
                return blocks;
            }

            final DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
            final Chest left = (Chest) doubleChest.getLeftSide();
            final Chest right = (Chest) doubleChest.getRightSide();

            blocks.clear();

            blocks.add(left.getBlock());
            blocks.add(right.getBlock());

            return blocks;
        }

        if (block.getType().equals(Material.BED) || block.getType().equals(Material.BED_BLOCK)) {
            final Bed bed = (Bed)block.getState().getData();
            blocks.add(block.getRelative(bed.getFacing()));
        }

        return blocks;
    }
}
