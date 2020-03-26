package com.llewkcor.ares.core.network.handlers;

import com.google.common.collect.Maps;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class NetworkManageHandler {
    @Getter public final NetworkHandler handler;
    @Getter public final Map<UUID, Long> renameCooldowns;

    public NetworkManageHandler(NetworkHandler handler) {
        this.handler = handler;
        this.renameCooldowns = Maps.newConcurrentMap();
    }

    public void deleteNetwork(Player player, String network, SimplePromise promise) {

    }

    /**
     * Handles a player leaving a network
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void leaveNetwork(Player player, String networkName, SimplePromise promise) {
        final Network network = handler.getManager().getNetworkByName(networkName);

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (networkMember.hasPermission(NetworkPermission.ADMIN) && network.getMembersWithPermission(NetworkPermission.ADMIN).size() <= 1) {
            promise.fail("There must be at least one other member with the ADMIN permission. Promote another member of disband the network.");
            return;
        }

        network.removeMember(player.getUniqueId());
        network.sendMessage(ChatColor.RED + player.getName() + " has left " + network.getName());
        promise.success();
    }

    public void kickFromNetwork(Player player, String network, String username, SimplePromise promise) {

    }

    public void renameNetwork(Player player, String network, String newName, SimplePromise promise) {

    }
}
