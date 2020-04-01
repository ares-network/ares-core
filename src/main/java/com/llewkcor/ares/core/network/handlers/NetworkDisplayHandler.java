package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.bridge.data.account.AresAccount;
import com.llewkcor.ares.core.network.NetworkHandler;
import com.llewkcor.ares.core.network.data.Network;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public final class NetworkDisplayHandler {
    @Getter public final NetworkHandler handler;

    /**
     * Handles printing display information for networks
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void printDisplay(Player player, String networkName, SimplePromise promise) {
        final Network network = handler.getManager().getNetworkByName(networkName);

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final UUID foundID = network.getCreatorId();
        final String foundedDate = Time.convertToDate(new Date(network.getCreateDate()));
        final int totalMembers = network.getMembers().size();
        final int onlineMembers = network.getOnlineMembers().size();
        final boolean passwordEnabled = network.getConfiguration().isPasswordEnabled();

        handler.getManager().getPlugin().getBridgeManager().getDataManager().getAccountByBukkitID(foundID, new FailablePromise<AresAccount>() {
            @Override
            public void success(AresAccount aresAccount) {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------");
                player.sendMessage(ChatColor.GOLD + network.getName());
                player.sendMessage(ChatColor.YELLOW + "Founded by " + aresAccount.getUsername() + " " + foundedDate);
                player.sendMessage(ChatColor.RESET + " ");
                player.sendMessage(ChatColor.YELLOW + "" + totalMembers + " (" + onlineMembers + " online)");
                player.sendMessage(ChatColor.YELLOW + "Password access is " + (passwordEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------");
                player.sendMessage(ChatColor.GOLD + network.getName());
                player.sendMessage(ChatColor.YELLOW + "Founded " + foundedDate);
                player.sendMessage(ChatColor.RESET + " ");
                player.sendMessage(ChatColor.YELLOW + "" + totalMembers + " (" + onlineMembers + " online)");
                player.sendMessage(ChatColor.YELLOW + "Password access is " + (passwordEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------");
            }
        });
    }

    /**
     * Handles printing a list of all networks the provided player is in
     * @param player Bukkit Player
     * @param promise Promise
     */
    public void printList(Player player, String username, SimplePromise promise) {
        final Collection<Network> networks = (username != null) ? handler.getManager().getNetworksByPlayer(username) : handler.getManager().getNetworksByPlayer(player);

        if (networks.isEmpty()) {
            promise.fail("You are not in any networks");
            return;
        }

        if (username != null) {
            player.sendMessage(ChatColor.GOLD + username + "'s Networks");
        } else {
            player.sendMessage(ChatColor.GOLD + "Your Networks");
        }

        networks.forEach(network -> player.sendMessage(ChatColor.GRAY + " - " + ChatColor.GOLD + network.getName() + ChatColor.DARK_AQUA + " (" + ChatColor.AQUA + network.getOnlineMembers().size() + "/" + network.getMembers().size() + " online" + ChatColor.DARK_AQUA + ")"));
        promise.success();
    }
}