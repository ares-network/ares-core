package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import com.llewkcor.ares.core.network.menu.NetworkPlayerMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkMenuHandler {
    @Getter public NetworkHandler handler;

    public void openPlayerEditMenu(Player player, String networkName, String memberName, SimplePromise promise) {
        final boolean admin = player.hasPermission("arescore.admin");
        final Network network = handler.getManager().getNetworkByName(networkName);

        if (network == null) {
            player.sendMessage(ChatColor.RED + "Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);
        final NetworkMember editedMember = network.getMember(memberName);

        if (networkMember != null && !networkMember.hasPermission(NetworkPermission.ADMIN) && !admin) {
            player.sendMessage(ChatColor.RED + "You do not have permission to perform this action");
            return;
        }

        if (editedMember == null) {
            promise.fail("Player not found");
            return;
        }

        final NetworkPlayerMenu menu = new NetworkPlayerMenu(handler.getManager().getPlugin(), network, editedMember, player);
        menu.open();
    }

    public void openConfigMenu(Player player, Network network) {

    }
}