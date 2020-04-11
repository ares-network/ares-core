package com.llewkcor.ares.core.factory;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.HelpCommand;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

            if (nextBlock == null || !(nextBlock.getType().equals(Material.CHEST))) {
                continue;
            }

            chest = nextBlock;

            // Checking that the workbench exists
            if (nextBlock.getType().equals(Material.CHEST)) {
                if (direction.equals(BlockFace.NORTH)) {
                    workbench = block.getRelative(BlockFace.SOUTH);

                    if (workbench == null || !workbench.getType().equals(Material.WORKBENCH)) {
                        promise.fail("Invalid factory configuration");
                        return;
                    }
                }

                else if (direction.equals(BlockFace.EAST)) {
                    workbench = block.getRelative(BlockFace.WEST);

                    if (workbench == null || !workbench.getType().equals(Material.WORKBENCH)) {
                        promise.fail("Invalid factory configuration");
                        return;
                    }
                }

                else if (direction.equals(BlockFace.SOUTH)) {
                    workbench = block.getRelative(BlockFace.NORTH);

                    if (workbench == null || !workbench.getType().equals(Material.WORKBENCH)) {
                        promise.fail("Invalid factory configuration");
                        return;
                    }
                }

                else if (direction.equals(BlockFace.WEST)) {
                    workbench = block.getRelative(BlockFace.EAST);

                    if (workbench == null || !workbench.getType().equals(Material.WORKBENCH)) {
                        promise.fail("Invalid factory configuration");
                        return;
                    }
                }
            }
        }

        // Chest or workbench did not exist
        if (chest == null || workbench == null) {
            promise.fail("Invalid factory configuration");
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

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
