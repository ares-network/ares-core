package com.playares.core.snitch;

import com.google.common.collect.Lists;
import com.playares.commons.location.BLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.services.account.AccountService;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.snitch.data.Snitch;
import com.playares.core.snitch.data.SnitchDAO;
import com.playares.core.snitch.data.SnitchEntry;
import com.playares.core.snitch.data.SnitchEntryType;
import com.playares.core.snitch.menu.SnitchListMenu;
import com.playares.core.snitch.menu.SnitchLogMenu;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SnitchHandler {
    @Getter public final SnitchManager manager;

    public SnitchHandler(SnitchManager manager) {
        this.manager = manager;
    }

    /**
     * Load all snitches from the MongoDB instance to memory
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all snitches from the database");
            manager.getSnitchRepository().addAll(SnitchDAO.getSnitches(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getSnitchRepository().size() + " Snitches");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getSnitchRepository().addAll(SnitchDAO.getSnitches(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getSnitchRepository().size() + " Snitches")).run();
        }).run();
    }

    /**
     * Save all snitches in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all snitches to the database");
            SnitchDAO.saveSnitches(manager.getPlugin().getDatabaseInstance(), manager.getSnitchRepository());
            Logger.print("Saved " + manager.getSnitchRepository().size() + " Snitches");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            SnitchDAO.saveSnitches(manager.getPlugin().getDatabaseInstance(), manager.getSnitchRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getSnitchRepository().size() + " Snitches")).run();
        }).run();
    }

    /**
     * Performs a scrub on all Snitch Entries to remove expired log entries
     */
    public void performEntryCleanup() {
        Logger.warn("Starting Snitch Log Cleanup...");

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getSnitchRepository().forEach(snitch -> {
                final List<SnitchEntry> toRemove = Lists.newArrayList();

                snitch.getLogEntries().forEach(entry -> {
                    if (entry.getExpireDate() <= Time.now()) {
                        toRemove.add(entry);
                    }
                });

                snitch.getLogEntries().removeAll(toRemove);
                SnitchDAO.saveSnitch(manager.getPlugin().getDatabaseInstance(), snitch);
            });

            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Completed Snitch Log Cleanup")).run();
        }).run();
    }

    /**
     * Deletes a snitch from the database
     * @param snitch Snitch
     */
    public void deleteSnitch(Snitch snitch) {
        manager.getSnitchRepository().remove(snitch);

        new Scheduler(getManager().getPlugin()).async(() -> {
            SnitchDAO.deleteSnitch(manager.getPlugin().getDatabaseInstance(), snitch);

            new Scheduler(getManager().getPlugin()).sync(() -> Logger.print("Deleted Snitch (" + snitch.getUniqueId().toString() + ") at " + snitch.getLocation().toString())).run();
        }).run();
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
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (profile == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (!profile.isSpawned()) {
            promise.fail("You have not spawned in yet");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (networkMember != null && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_SNITCHES)) && !admin) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final Snitch existingSnitch = getManager().getSnitchByBlock(block);

        if (existingSnitch != null) {
            promise.fail("This block is already a snitch");
            return;
        }

        final Snitch snitch = new Snitch(description, network.getUniqueId(), block, (Time.now() + (getManager().getPlugin().getConfigManager().getSnitchesConfig().getExpireTime() * 1000)));
        manager.getSnitchRepository().add(snitch);
        network.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.YELLOW + " created a new " + ChatColor.AQUA + "Snitch Block" + ChatColor.YELLOW + " at " + ChatColor.GREEN + snitch.getLocation().toString());
        player.sendMessage(ChatColor.AQUA + "Snitch" + ChatColor.YELLOW + " will mature in " + ChatColor.GOLD + Time.convertToRemaining(snitch.getMatureTime() - Time.now()));
        Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") created a snitch (" + snitch.getUniqueId().toString() + ") at " + snitch.getLocation().toString());
        promise.success();
    }

    /**
     * Handles triggering a snitch
     * @param snitch Snitch block
     * @param player Triggering player
     * @param locatable Trigger location
     * @param material Material (if applicable)
     * @param type SnitchEntryType
     */
    public void triggerSnitch(Snitch snitch, Player player, BLocatable locatable, Material material, SnitchEntryType type) {
        final AccountService service = (AccountService)manager.getPlugin().getService(AccountService.class);
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(snitch.getOwnerId());

        if (service == null) {
            Logger.error("Account Service was not found while attempting to trigger a snitch at " + snitch.getLocation().toString());
            return;
        }

        final SnitchEntry entry = new SnitchEntry(
                type,
                (player != null ? player.getName() : null),
                material.name(),
                type.getDisplayName(),
                locatable,
                (Time.now() + (manager.getPlugin().getConfigManager().getSnitchesConfig().getLogEntryExpireSeconds() * 1000)));

        snitch.getLogEntries().add(entry);

        final String notification = formatNotification(snitch, entry);

        if (player != null && notification != null && network.getConfiguration().isSnitchNotificationsEnabled()) {
            final List<NetworkMember> receivers = Lists.newArrayList();

            receivers.addAll(network.getOnlineMembers()
                    .stream()
                    .filter(member -> member.hasPermission(NetworkPermission.VIEW_SNITCHES) || member.hasPermission(NetworkPermission.ADMIN))
                    .collect(Collectors.toList()));

            receivers.stream().filter(receiver -> receiver.getBukkitPlayer() != null).forEach(receiver -> {
                final AresPlayer receiverProfile = manager.getPlugin().getPlayerManager().getPlayer(receiver.getUniqueId());

                if (receiverProfile != null && receiverProfile.getSettings().isSnitchNotificationsEnabled()) {
                    receiver.getBukkitPlayer().sendMessage(notification);
                }
            });
        }
    }

    /**
     * Handles clearing the log entries for a snitch
     * @param player Player
     * @param block Snitch Block
     * @param promise Promise
     */
    public void clearLogs(Player player, Block block, SimplePromise promise) {
        final Snitch snitch = getManager().getSnitchByBlock(block);
        final boolean admin = player.hasPermission("arescore.admin");

        if (snitch == null) {
            promise.fail("This block is not a snitch");
            return;
        }

        final Network network = getManager().getPlugin().getNetworkManager().getNetworkByID(snitch.getOwnerId());

        if (network == null) {
            promise.fail("Network not found for this snitch");
            return;
        }

        if (!network.isMember(player) && !admin) {
            promise.fail("You do not have ownership of this snitch");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember != null && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.VIEW_SNITCHES)) && !admin) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        snitch.getLogEntries().clear();
        Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") cleared the logs for a snitch owned by " + network.getName() + "(" + network.getUniqueId().toString() + ") at " + snitch.getLocation().toString());
        promise.success();
    }

    /**
     * Handles printing log information for a Snitch
     * @param player Player
     * @param block Snitch Block
     * @param promise Promise
     */
    public void viewLogs(Player player, Block block, SimplePromise promise) {
        final Snitch snitch = getManager().getSnitchByBlock(block);
        final boolean admin = player.hasPermission("arescore.admin");

        if (snitch == null) {
            promise.fail("This block is not a snitch");
            return;
        }

        final Network network = getManager().getPlugin().getNetworkManager().getNetworkByID(snitch.getOwnerId());

        if (network == null) {
            promise.fail("Network not found for this snitch");
            return;
        }

        if (!network.isMember(player) && !admin) {
            promise.fail("You do not have ownership of this snitch");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember != null && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.VIEW_SNITCHES)) && !admin) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final List<SnitchEntry> entries = Lists.newArrayList(snitch.getSortedEntries());

        if (snitch.getLogEntries().isEmpty() || entries.isEmpty()) {
            promise.fail("Could not find any records within the provided timeframe");
            return;
        }

        final SnitchLogMenu menu = new SnitchLogMenu(manager.getPlugin(), player, snitch);
        menu.open();
        promise.success();
    }

    /**
     * Handles displaying a GUI of all snitches within a 64 block range of the player
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void listByNetwork(Player player, String networkName, SimplePromise promise) {
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember member = network.getMember(player);

        if (member == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(member.hasPermission(NetworkPermission.ADMIN) || member.hasPermission(NetworkPermission.VIEW_SNITCHES))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final List<Snitch> snitches = manager.getSnitchByOwner(network);

        if (snitches.isEmpty()) {
            promise.fail("There are no snitches nearby");
            return;
        }

        final SnitchListMenu menu = new SnitchListMenu(manager.getPlugin(), player, "Snitches: " + network.getName(), snitches);
        menu.open();
        promise.success();
    }

    /**
     * Handles opening a GUI containing all nearby friendly snitches
     * @param player Player
     * @param promise Promise
     */
    public void listByNearby(Player player, SimplePromise promise) {
        final UUID uniqueId = player.getUniqueId();

        new Scheduler(manager.getPlugin()).async(() -> {
            final List<Snitch> nearby = manager.getSnitchByRadius(new BLocatable(player.getLocation().getBlock()), 64.0);
            final List<Snitch> friendly = nearby.stream().filter(snitch -> manager.getPlugin().getNetworkManager().getNetworkByID(snitch.getOwnerId()).isMember(uniqueId)).collect(Collectors.toList());

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (friendly.isEmpty()) {
                    promise.fail("There are no snitches nearby");
                    return;
                }

                final SnitchListMenu menu = new SnitchListMenu(manager.getPlugin(), player, "Nearby Snitches", friendly);
                menu.open();
                promise.success();
            }).run();
        }).run();
    }

    private String formatNotification(Snitch snitch, SnitchEntry entry) {
        if (entry.getType().equals(SnitchEntryType.LOGIN)) {
            return ChatColor.WHITE + "* " + ChatColor.AQUA + entry.getEntity() + " connected at " + snitch.getName() + " [" + snitch.getLocation().getX() + ", " + snitch.getLocation().getY() + ", " + snitch.getLocation().getZ() + ", " + snitch.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase() + "]";
        }

        if (entry.getType().equals(SnitchEntryType.LOGOUT)) {
            return ChatColor.WHITE + "* " + ChatColor.AQUA + entry.getEntity() + " disconnected at " + snitch.getName() + " [" + snitch.getLocation().getX() + ", " + snitch.getLocation().getY() + ", " + snitch.getLocation().getZ() + ", " + snitch.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase() + "]";
        }

        if (entry.getType().equals(SnitchEntryType.SPOTTED)) {
            return ChatColor.WHITE + "* " + ChatColor.AQUA + entry.getEntity() + " was spotted at " + snitch.getName() + " [" + snitch.getLocation().getX() + ", " + snitch.getLocation().getY() + ", " + snitch.getLocation().getZ() + ", " + snitch.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase() + "]";
        }

        if (entry.getType().equals(SnitchEntryType.KILL)) {
            return ChatColor.WHITE + "* " + ChatColor.AQUA + entry.getEntity() + " died at " + snitch.getName() + " [" + snitch.getLocation().getX() + ", " + snitch.getLocation().getY() + ", " + snitch.getLocation().getZ() + ", " + snitch.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase() + "]";
        }

        return null;
    }
}