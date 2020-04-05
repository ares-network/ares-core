package com.llewkcor.ares.core.acid;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.acid.data.AcidBlock;
import com.llewkcor.ares.core.acid.data.AcidDAO;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class AcidHandler {
    @Getter public final AcidManager manager;

    /**
     * Load all Acid Blocks from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all acid blocks from the database");
            manager.getAcidRepository().addAll(AcidDAO.getAcidBlocks(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getAcidRepository().size() + " Acid Blocks");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getAcidRepository().addAll(AcidDAO.getAcidBlocks(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getAcidRepository().size() + " Acid Blocks")).run();
        }).run();
    }

    /**
     * Save all Acid Blocks in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all acid blocks to the database");
            AcidDAO.saveAcidBlocks(manager.getPlugin().getDatabaseInstance(), manager.getAcidRepository());
            Logger.print("Saved " + manager.getAcidRepository().size() + " Acid Blocks");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            AcidDAO.saveAcidBlocks(manager.getPlugin().getDatabaseInstance(), manager.getAcidRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getAcidRepository().size() + " Acid Blocks")).run();
        }).run();
    }

    /**
     * Performs a database cleanup for Expired Acid Blocks
     */
    public void performAcidCleanup() {
        Logger.warn("Starting Acid Cleanup...");

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getExpiredAcidBlocks().forEach(expired -> {
                manager.getAcidRepository().remove(expired);
                AcidDAO.deleteAcidBlock(manager.getPlugin().getDatabaseInstance(), expired);
            });

            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Completed Acid Cleanup")).run();
        }).run();
    }

    /**
     * Handles creating a new Acid Block
     * @param player Player
     * @param networkName Network Name
     * @param block Block
     * @param promise Promise
     */
    public void createAcid(Player player, String networkName, Block block, SimplePromise promise) {
        final boolean admin = player.hasPermission("arescore.admin");
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final BLocatable location = new BLocatable(block);

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_ACID))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final AcidBlock existing = manager.getAcidBlockByBlock(location);

        if (existing != null) {
            promise.fail("This block is already an acid block");
            return;
        }

        final AcidBlock acidBlock = new AcidBlock(network, block, (Time.now() + (manager.getPlugin().getConfigManager().getAcidConfig().getAcidMatureTime() * 1000L)), (Time.now() + (manager.getPlugin().getConfigManager().getAcidConfig().getAcidExpireTime() * 1000L)));

        manager.getAcidRepository().add(acidBlock);
        new Scheduler(manager.getPlugin()).async(() -> AcidDAO.saveAcidBlock(manager.getPlugin().getDatabaseInstance(), acidBlock)).run();

        player.sendMessage(ChatColor.YELLOW + "This Acid Block will mature in " + Time.convertToRemaining(acidBlock.getMatureTime() - Time.now()));

        promise.success();
    }

    /**
     * Handles deleting an Acid Block
     * @param acidBlock Acid Block
     */
    public void deleteAcid(AcidBlock acidBlock) {
        manager.getAcidRepository().remove(acidBlock);
        new Scheduler(manager.getPlugin()).async(() -> AcidDAO.deleteAcidBlock(manager.getPlugin().getDatabaseInstance(), acidBlock));
    }

    /**
     * Handles proving lookup info for an Acid Block
     * @param player Player
     * @param block Block
     * @param promise Promise
     */
    public void lookupAcid(Player player, Block block, SimplePromise promise) {
        final AcidBlock acid = manager.getAcidBlockByBlock(new BLocatable(block));

        if (acid == null) {
            promise.fail("This block is not an acid block");
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(acid.getOwnerId());

        if (network == null) {
            promise.fail("There was an unexpected error");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Acid claimed by " + network.getName() + ", " + (acid.isMature() ? "matured." : "matures in " + Time.convertToRemaining(acid.getMatureTime() - Time.now())));
        player.sendMessage(ChatColor.YELLOW + "Expires in " + Time.convertToRemaining(acid.getExpireTime() - Time.now()));
        promise.success();
    }
}
