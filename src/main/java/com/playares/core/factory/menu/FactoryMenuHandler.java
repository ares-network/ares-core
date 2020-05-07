package com.playares.core.factory.menu;

import com.playares.commons.promise.SimplePromise;
import com.playares.core.factory.FactoryManager;
import com.playares.core.factory.data.Factory;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

@AllArgsConstructor
public final class FactoryMenuHandler {
    @Getter public FactoryManager manager;

    /**
     * Handles opening the Factory Recipe menu for the provided Factory
     * @param player Player
     * @param factory Factory
     */
    public void openFactoryRecipes(Player player, Factory factory) {
        final FactoryRecipeMenu menu = new FactoryRecipeMenu(manager.getPlugin(), player, factory);
        menu.open();
    }

    /**
     * Handles opening the Factory job menu for the provided Factory
     * @param player Player
     * @param factory Factory
     */
    public void openFactoryJobs(Player player, Factory factory) {
        final FactoryJobMenu menu = new FactoryJobMenu(manager.getPlugin(), player, factory);
        menu.open();
    }

    /**
     * Handles opening a GUI of factories for the provided network
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

        if (!admin && !(member.hasPermission(NetworkPermission.ADMIN) || member.hasPermission(NetworkPermission.ACCESS_FACTORY))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final Set<Factory> factories = manager.getFactoryByOwner(network);

        if (factories.isEmpty()) {
            promise.fail("This network does not have any active factories");
            return;
        }

        final FactoryListMenu menu = new FactoryListMenu(manager.getPlugin(), player, "Factories: " + network.getName(), factories);
        menu.open();
        promise.success();
    }
}