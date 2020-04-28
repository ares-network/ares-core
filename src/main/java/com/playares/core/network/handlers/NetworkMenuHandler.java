package com.playares.core.network.handlers;

import com.playares.commons.promise.SimplePromise;
import com.playares.core.network.NetworkHandler;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.network.menu.NetworkConfigMenu;
import com.playares.core.network.menu.NetworkPlayerListMenu;
import com.playares.core.network.menu.NetworkPlayerMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkMenuHandler {
    @Getter public NetworkHandler handler;

    /**
     * Handles opening a player list menu for the provided network name
     * @param player Player
     * @param networkName Network name
     * @param promise Promise
     */
    public void openPlayerListMenu(Player player, String networkName, SimplePromise promise) {
        final Network network = handler.getManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember member = network.getMember(player);

        if (member == null && !admin) {
            promise.fail("You are not a member of " + network.getName());
            return;
        }

        final NetworkPlayerListMenu menu = new NetworkPlayerListMenu(handler.getManager().getPlugin(), player, network);
        menu.open();
        promise.success();
    }

    /**
     * Handles opening the player menu for the provided network name
     * @param player Player
     * @param networkName Network name
     * @param memberName Network member name
     * @param promise Promise
     */
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

        if (editedMember.getUniqueId().equals(player.getUniqueId())) {
            promise.fail("You can not edit your own permissions");
            return;
        }

        final NetworkPlayerMenu menu = new NetworkPlayerMenu(handler.getManager().getPlugin(), network, editedMember, player);
        menu.open();
    }

    /**
     * Handles opening the config menu for the provided network name
     * @param player Player
     * @param networkName Network name
     * @param promise Promise
     */
    public void openConfigMenu(Player player, String networkName, SimplePromise promise) {
        final boolean admin = player.hasPermission("arescore.admin");
        final Network network = handler.getManager().getNetworkByName(networkName);

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember != null && !networkMember.hasPermission(NetworkPermission.ADMIN) && !admin) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final NetworkConfigMenu menu = new NetworkConfigMenu(handler.getManager().getPlugin(), network, player);
        menu.open();
    }
}