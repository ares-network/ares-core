package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.bridge.data.account.AresAccount;
import com.llewkcor.ares.core.network.NetworkHandler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
        final Network network = handler.getManager().getNetworkByName(networkName);

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

        if (!networkMember.hasPermission(NetworkPermission.INVITE_MEMBERS) && !networkMember.hasPermission(NetworkPermission.ADMIN)) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        handler.getManager().getPlugin().getBridgeManager().getDataManager().getAccountByUsername(username, new FailablePromise<AresAccount>() {
            @Override
            public void success(AresAccount aresAccount) {
                if (network.getPendingMembers().contains(aresAccount.getBukkitId())) {
                    promise.fail(aresAccount.getUsername() + " already has a pending invitation to join this network");
                    return;
                }

                network.sendMessage(ChatColor.YELLOW + player.getName() + " invited " + aresAccount.getUsername() + " to " + network.getName());
                Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") invited " + aresAccount.getUsername() + "(" + aresAccount.getBukkitId().toString() + ") to " + network.getName() + "(" + network.getUniqueId().toString() + ")");

                if (aresAccount.getSettings().isAutoAcceptNetworkInvites() && network.getMembers().size() < handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkMembers()) {
                    network.addMember(aresAccount.getUniqueId(), aresAccount.getUsername());
                    network.sendMessage(ChatColor.GREEN + aresAccount.getUsername() + " has joined " + network.getName());
                    return;
                }

                network.getPendingMembers().add(aresAccount.getUniqueId());

                final Player invitedPlayer = Bukkit.getPlayer(aresAccount.getBukkitId());

                if (invitedPlayer != null && invitedPlayer.isOnline()) {
                    invitedPlayer.sendMessage(ChatColor.GREEN + "You have been invited to join " + network.getName() + ". Type '/network accept " + network.getName() + "' to join");
                }
            }

            @Override
            public void fail(String s) {
                promise.fail(s);
            }
        });
    }

    public void uninviteMember(Player player, String network, String username, SimplePromise promise) {

    }

    public void acceptInvite(Player player, String network, SimplePromise promise) {

    }
}
