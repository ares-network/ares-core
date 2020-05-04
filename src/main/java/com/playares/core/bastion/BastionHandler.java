package com.playares.core.bastion;

import com.google.common.collect.ImmutableSet;
import com.playares.commons.location.BLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.bastion.data.Bastion;
import com.playares.core.bastion.data.BastionDAO;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.player.data.AresPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class BastionHandler {
    @Getter public final BastionManager manager;

    /**
     * Load all bastions from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all bastions from the database");
            manager.getBastionRepository().addAll(BastionDAO.getBastions(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getBastionRepository().size() + " Bastions");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getBastionRepository().addAll(BastionDAO.getBastions(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getBastionRepository().size() + " Bastions")).run();
        }).run();
    }

    /**
     * Save all bastions in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all bastions to the database");
            BastionDAO.saveBastions(manager.getPlugin().getDatabaseInstance(), manager.getBastionRepository());
            Logger.print("Saved " + manager.getBastionRepository().size() + " Bastions");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            BastionDAO.saveBastions(manager.getPlugin().getDatabaseInstance(), manager.getBastionRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getBastionRepository().size() + " Bastions")).run();
        }).run();
    }

    /**
     * Handles creating a new bastion
     * @param player Creator
     * @param networkName Network Name
     * @param block Bastion Block
     * @param promise Promise
     */
    public void createBastion(Player player, String networkName, Block block, SimplePromise promise) {
        final AresPlayer account = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("arescore.admin");
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final BLocatable location = new BLocatable(block);

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

        final NetworkMember member = network.getMember(player);

        if (member == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(member.hasPermission(NetworkPermission.ADMIN) || member.hasPermission(NetworkPermission.MODIFY_BASTION))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final Bastion existing = manager.getBastionByBlock(new BLocatable(block));

        if (existing != null) {
            promise.fail("This block is already a bastion block");
            return;
        }

        final ImmutableSet<Bastion> inRadius = manager.getBastionInRangeFlat(location, manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius() * 2);

        if (!inRadius.isEmpty()) {
            promise.fail("This bastion is too close to another bastion");
            return;
        }

        final Bastion bastion = new Bastion(network.getUniqueId(), new BLocatable(block), (Time.now() + (manager.getPlugin().getConfigManager().getBastionsConfig().getBastionMatureTime() * 1000L)));
        manager.getBastionRepository().add(bastion);
        new Scheduler(manager.getPlugin()).async(() -> BastionDAO.saveBastion(manager.getPlugin().getDatabaseInstance(), bastion)).run();

        network.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.YELLOW + " created a " + ChatColor.LIGHT_PURPLE + "Bastion Block" + ChatColor.YELLOW + " at " + ChatColor.GREEN + bastion.getLocation().toString());
        player.sendMessage(ChatColor.YELLOW + "This bastion will mature in " + Time.convertToRemaining(bastion.getMatureTime() - Time.now()));
        Logger.print(player.getName() + " (" + player.getUniqueId().toString() + ") created a bastion at " + bastion.getLocation().toString() + " for " + network.getName() + " (" + network.getUniqueId().toString() + ")");

        promise.success();
    }

    /**
     * Handles deleting a Bastion
     * @param bastion Bastion
     */
    public void deleteBastion(Bastion bastion) {
        manager.getBastionRepository().remove(bastion);
        new Scheduler(manager.getPlugin()).async(() -> BastionDAO.deleteBastion(manager.getPlugin().getDatabaseInstance(), bastion)).run();
    }

    /**
     * Handles showing a list of all nearby bastions for the provided Player
     * @param player Player
     * @param promise Promise
     */
    public void showNear(Player player, SimplePromise promise) {
        final ImmutableSet<Bastion> nearby = manager.getBastionInRangeFlat(new BLocatable(player.getLocation().getBlock()), manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

        if (nearby.isEmpty()) {
            promise.fail("No bastions nearby");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Nearby Bastions");
        nearby.forEach(bastion -> {
            final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());
            final long timeUntilMature = bastion.getMatureTime() - Time.now();

            if (owner != null) {
                player.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + owner.getName() + ": " + ChatColor.BLUE + bastion.getLocation().toString().replace(",", ChatColor.YELLOW + "," + ChatColor.BLUE));

                if (!bastion.isMature()) {
                    player.sendMessage(ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.RED + "Matures in " + Time.convertToRemaining(timeUntilMature));
                }
            } else {
                Logger.error("Found a bastion with no defined owner");
            }
        });
        promise.success();
    }

    /**
     * Handles printing info for a bastion
     * @param player Player
     * @param block Block
     * @param promise Promise
     */
    public void showInfo(Player player, Block block, SimplePromise promise) {
        final Bastion bastion = manager.getBastionByBlock(new BLocatable(block));

        if (bastion == null) {
            promise.fail("This block is not a bastion");
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

        if (network == null) {
            promise.fail("There was an unexpected error");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Bastion claimed by " + network.getName() + ", " + (bastion.isMature() ? "matured." : "matures in " + Time.convertToRemaining(bastion.getMatureTime() - Time.now())));
        promise.success();
    }
}