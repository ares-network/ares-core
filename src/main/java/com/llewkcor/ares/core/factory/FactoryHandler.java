package com.llewkcor.ares.core.factory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Blocks;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryDAO;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public final class FactoryHandler {
    @Getter public final FactoryManager manager;

    /**
     * Load all factories from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all factories from the database");
            manager.getFactoryRepository().addAll(FactoryDAO.getFactories(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getFactoryRepository().size() + " Factories");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getFactoryRepository().addAll(FactoryDAO.getFactories(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getFactoryRepository().size() + " Factories")).run();
        }).run();
    }

    /**
     * Save all factories in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all factories to the database");
            FactoryDAO.saveFactories(manager.getPlugin().getDatabaseInstance(), manager.getFactoryRepository());
            Logger.print("Saved " + manager.getFactoryRepository().size() + " Factories");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            FactoryDAO.saveFactories(manager.getPlugin().getDatabaseInstance(), manager.getFactoryRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getFactoryRepository().size() + " Factories")).run();
        }).run();
    }

    /**
     * Handles creating a new Factory
     * @param player Player
     * @param block Furnace Block
     * @param networkName Network Name
     * @param promise Promise
     */
    public void createFactory(Player player, Block block, String networkName, SimplePromise promise) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");
        Block workbench = null;
        Block chest = null;

        if (account == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (!account.isSpawned()) {
            promise.fail("You have not spawned in yet");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember != null && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_FACTORY)) && !admin) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        for (BlockFace direction : Blocks.getFlatDirections()) {
            final Block nextBlock = block.getRelative(direction);
            final Block opposite = block.getRelative(direction.getOppositeFace());

            if (nextBlock == null || !(nextBlock.getType().equals(Material.CHEST) || nextBlock.getType().equals(Material.TRAPPED_CHEST))) {
                continue;
            }

            chest = nextBlock;

            // Checking that the workbench exists
            if (opposite != null && opposite.getType().equals(Material.WORKBENCH)) {
                workbench = opposite;
                break;
            }
        }

        // Chest or workbench did not exist
        if (chest == null || workbench == null) {
            final List<String> notFound = Lists.newArrayList();

            if (chest == null) {
                notFound.add("Chest");
            }

            if (workbench == null) {
                notFound.add("Crafting Bench");
            }

            promise.fail("Invalid factory configuration: " + Joiner.on(" & ").join(notFound) + " not found");
            return;
        }

        final Factory existing = manager.getFactoryByBlock(new BLocatable(block));
        final Claim furnaceClaim = manager.getPlugin().getClaimManager().getClaimByBlock(block);
        final Claim chestClaim = manager.getPlugin().getClaimManager().getClaimByBlock(chest);
        final Claim workbenchClaim = manager.getPlugin().getClaimManager().getClaimByBlock(workbench);

        if (existing != null) {
            promise.fail("This block is already a part of a Factory configuration");
            return;
        }

        if (furnaceClaim != null && !furnaceClaim.getOwnerId().equals(network.getUniqueId())) {
            promise.fail("Furnace block is not owned by " + network.getName());
            return;
        }

        if (chestClaim != null && !chestClaim.getOwnerId().equals(network.getUniqueId())) {
            promise.fail("Chest block is not owned by " + network.getName());
            return;
        }

        if (workbenchClaim != null && !workbenchClaim.getOwnerId().equals(network.getUniqueId())) {
            promise.fail("Workbench block is not owned by " + network.getName());
            return;
        }

        final Factory factory = new Factory(network.getUniqueId(), chest, block, workbench);
        manager.getFactoryRepository().add(factory);

        network.sendMessage(ChatColor.YELLOW + player.getName() + " has created a factory for " + network.getName() + " at " + factory.getFurnaceLocation().toString());

        promise.success();
    }
}
