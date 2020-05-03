package com.playares.core.network.handlers;

import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.services.account.AccountService;
import com.playares.core.network.NetworkHandler;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.player.data.AresPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkInviteHandler {
    @Getter public final NetworkHandler handler;

    /**
     * Sends a pending invitation to join a network to the named player for the named network
     * @param player Inviting Player
     * @param networkName Network Name
     * @param username Invited Username
     * @param promise Promise
     */
    public void inviteMember(Player player, String networkName, String username, SimplePromise promise) {
        final AccountService service = (AccountService)handler.getManager().getPlugin().getService(AccountService.class);
        final Network network = handler.getManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (service == null) {
            promise.fail("Failed to obtain Account Service");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        if (network.isMember(username)) {
            promise.fail("This player is already a part of this network");
            return;
        }

        final NetworkMember networkMember = network.getMembers().stream().filter(member -> member.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);

        if (networkMember == null) {
            promise.fail("There was an issue locating your account information");
            return;
        }

        if (!admin && !(networkMember.hasPermission(NetworkPermission.INVITE_MEMBERS) || networkMember.hasPermission(NetworkPermission.ADMIN))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        service.getAccountByUsername(username, aresAccount -> {
            if (aresAccount == null) {
                promise.fail("Account not found");
                return;
            }

            final AresPlayer account = handler.getManager().getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

            if (network.getPendingMembers().contains(aresAccount.getBukkitId())) {
                promise.fail(aresAccount.getUsername() + " already has a pending invitation to join this network");
                return;
            }

            network.sendMessage(ChatColor.YELLOW + player.getName() + " invited " + aresAccount.getUsername() + " to " + network.getName());
            Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") invited " + aresAccount.getUsername() + "(" + aresAccount.getBukkitId().toString() + ") to " + network.getName() + "(" + network.getUniqueId().toString() + ")");

            if (
                // Offline user is accepting automatically
                    account.getSettings().isAutoAcceptNetworkInvites()
                            // Network has enough room for them to join
                            && network.getMembers().size() < handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkMembers()
                            // Player is not in too many networks
                            && handler.getManager().getNetworksByPlayer(aresAccount.getBukkitId()).size() < handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxJoinedNetworks()) {

                network.addMember(aresAccount.getUniqueId(), aresAccount.getUsername());
                network.sendMessage(ChatColor.GREEN + aresAccount.getUsername() + " has joined " + network.getName());
                return;

            }

            network.getPendingMembers().add(aresAccount.getBukkitId());

            final Player invitedPlayer = Bukkit.getPlayer(aresAccount.getBukkitId());

            if (invitedPlayer != null && invitedPlayer.isOnline()) {
                invitedPlayer.spigot().sendMessage(new ComponentBuilder
                        ("You have been invited to join ").color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .append(network.getName()).color(net.md_5.bungee.api.ChatColor.BLUE)
                        .append(". Type ").color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .append("/network accept " + network.getName()).color(net.md_5.bungee.api.ChatColor.GOLD)
                        .append(" or ").color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .append("[Click Here]").color(net.md_5.bungee.api.ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/network accept " + network.getName()))
                        .append(" to join.").color(net.md_5.bungee.api.ChatColor.YELLOW).create());
            }
        });
    }

    /**
     * Revokes an existing invitation to join a network
     * @param player Revoking Player
     * @param networkName Network Name
     * @param username Revoked Username
     * @param promise Promise
     */
    public void uninviteMember(Player player, String networkName, String username, SimplePromise promise) {
        final AccountService service = (AccountService)handler.getManager().getPlugin().getService(AccountService.class);
        final Network network = handler.getManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (service == null) {
            promise.fail("Failed to obtain Account Service");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMembers().stream().filter(member -> member.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);

        if (networkMember == null || !network.isMember(player) && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(networkMember.hasPermission(NetworkPermission.INVITE_MEMBERS) || networkMember.hasPermission(NetworkPermission.ADMIN))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        service.getAccountByUsername(username, aresAccount -> {
            if (aresAccount == null) {
                promise.fail("Account not found");
                return;
            }

            if (!network.getPendingMembers().contains(aresAccount.getBukkitId())) {
                promise.fail("Player did not have a pending invitation to join this network");
                return;
            }

            network.getPendingMembers().remove(aresAccount.getBukkitId());
            network.sendMessage(ChatColor.YELLOW + player.getName() + " revoked " + aresAccount.getUsername() + "'s invitation to join " + network.getName());
            Logger.print(player.getName() + " revoked " + aresAccount.getUsername() + "'s invitation to join " + network.getName() + "(" + network.getUniqueId().toString() + ")");
            promise.success();
        });
    }

    /**
     * Accept an invitation to join a network
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void acceptInvite(Player player, String networkName, SimplePromise promise) {
        final Network network = handler.getManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (handler.getManager().getNetworksByPlayer(player).size() >= handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxJoinedNetworks() && !admin) {
            promise.fail("You have joined the max amount of networks per account. Leave other networks you're in and try to accept this invitation again");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        if (!network.getPendingMembers().contains(player.getUniqueId()) && !admin) {
            promise.fail("You have not been invited to this network");
            return;
        }

        if (network.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(player.getUniqueId())) && !admin) {
            promise.fail("You are already a member of this network");
            return;
        }

        if (network.getMembers().size() >= handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkMembers() && !admin) {
            promise.fail("Network is full");
            return;
        }

        network.addMember(player);
        network.getPendingMembers().remove(player.getUniqueId());
        network.sendMessage(ChatColor.GREEN + player.getName() + " has joined " + network.getName());
        Logger.print(player.getName());
        promise.success();
    }

    /**
     * Accept an invitation to join a network via password
     * @param player Player
     * @param networkName Network Name
     * @param password Network Password
     * @param promise Promise
     */
    public void acceptInvite(Player player, String networkName, String password, SimplePromise promise) {
        final Network network = handler.getManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (handler.getManager().getNetworksByPlayer(player).size() >= handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxJoinedNetworks() && !admin) {
            promise.fail("You have joined the max amount of networks per account. Leave other networks you're in and try to accept this invitation again");
            return;
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        if (network.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(player.getUniqueId()))) {
            promise.fail("You are already a member of this network");
            return;
        }

        if (network.getMembers().size() >= handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkMembers() && !admin) {
            promise.fail("Network is full");
            return;
        }

        if (!network.getConfiguration().isPasswordEnabled()) {
            promise.fail("Password access is not allowed for this network");
            return;
        }

        if (!password.equalsIgnoreCase(network.getConfiguration().getPassword())) {
            promise.fail("Incorrect password");
            return;
        }

        network.addMember(player);
        network.getPendingMembers().remove(player.getUniqueId());
        network.sendMessage(ChatColor.GREEN + player.getName() + " has joined " + network.getName());
        Logger.print(player.getName());
        promise.success();
    }
}