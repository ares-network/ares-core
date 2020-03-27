package com.llewkcor.ares.core.snitch;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.bridge.data.account.AresAccount;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.llewkcor.ares.core.snitch.data.SnitchDAO;
import com.llewkcor.ares.core.snitch.data.SnitchEntry;
import com.llewkcor.ares.core.snitch.data.SnitchEntryType;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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
            Logger.warn("Blocking the thread while attempting to load all networks from the database");
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
            Logger.warn("Blocking the thread while attempting to save all networks to the database");
            SnitchDAO.saveSnitches(manager.getPlugin().getDatabaseInstance(), manager.getSnitchRepository());
            Logger.print("Saved " + manager.getSnitchRepository().size() + " Networks");
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
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (networkMember != null && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_SNITCHES))) {
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

        player.sendMessage(ChatColor.YELLOW + "Snitch will mature in " + Time.convertToRemaining(snitch.getMatureTime() - Time.now()));

        Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") created a snitch (" + snitch.getUniqueId().toString() + ") at " + snitch.getLocation().toString());

        promise.success();
    }

    /**
     * Trigger a snitch
     * @param snitch Snitch
     * @param player Player involved
     * @param block Block
     * @param type Snitch Entry Type
     */
    public void triggerSnitch(Snitch snitch, Player player, Block block, SnitchEntryType type) {
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(snitch.getOwnerId());

        final SnitchEntry entry = new SnitchEntry(
                type,
                (player != null ? player.getName() : null),
                block.getType().name(),
                type.getDisplayName(),
                new BLocatable(block),
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
                final AresAccount receiverAccount = manager.getPlugin().getBridgeManager().getDataManager().getAccountByBukkitID(receiver.getUniqueId());

                if (receiverAccount != null && receiverAccount.getSettings().isSnitchNotificationsEnabled()) {
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
     * @param page Log Page
     * @param promise Promise
     */
    public void viewLogs(Player player, Block block, int page, SimplePromise promise) {
        final Snitch snitch = getManager().getSnitchByBlock(block);
        final boolean admin = player.hasPermission("arescore.admin");

        // Fix 0 display
        if (page < 1) {
            page = 1;
        }

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

        final List<SnitchEntry> entries = Lists.newArrayList(snitch.getSortedEntries());

        if (snitch.getLogEntries().isEmpty() || entries.isEmpty()) {
            promise.fail("Could not find any records within the provided timeframe");
            return;
        }

        final int start = (page > 1) ? ((page - 1) * 10) : 0;
        final int end = (start + 10);
        final int totalPages = Math.round(entries.size() / 10);

        Collections.reverse(entries);

        for (int i = start; i < end; i++) {
            if (entries.size() <= end) {
                break;
            }

            final SnitchEntry entry = entries.get(i);

            switch(entry.getType()) {
                case BLOCK_BREAK : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.GREEN + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getBlock() + ChatColor.GRAY + " broken by " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case BLOCK_PLACE : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.GREEN + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getBlock() + ChatColor.GRAY + " placed by " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case BLOCK_INTERACTION : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.GREEN + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getBlock() + ChatColor.GRAY + " interacted with by " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case KILL : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.DARK_RED + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " died at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case LOGIN : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.YELLOW + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " connected at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case LOGOUT : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.YELLOW + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " disconnected at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
                case SPOTTED : player.sendMessage(ChatColor.GRAY + " * " + ChatColor.DARK_AQUA + "[" + (i + 1) + "] " + ChatColor.LIGHT_PURPLE + entry.getType().getDisplayName() + ChatColor.GRAY + " - " + ChatColor.BLUE + entry.getEntity() + ChatColor.GRAY + " seen at X: " + ChatColor.AQUA + entry.getBlockLocation().getX() + ChatColor.GRAY + ", Y: " + ChatColor.AQUA + entry.getBlockLocation().getY() + ChatColor.GRAY + ", Z: " + ChatColor.AQUA + entry.getBlockLocation().getZ());
                    break;
            }

            player.sendMessage(ChatColor.GRAY + "At " + ChatColor.GOLD + Time.convertToDate(new Date(entry.getCreatedDate())) + ChatColor.GRAY + " (" + Time.convertToElapsed(Time.now() - entry.createdDate) + ")");
            player.sendMessage(ChatColor.RESET + " ");
        }

        player.sendMessage(ChatColor.GOLD + "Page " + ChatColor.YELLOW + page + "/" + (totalPages));

        promise.success();
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