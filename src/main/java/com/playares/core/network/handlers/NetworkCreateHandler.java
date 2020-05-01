package com.playares.core.network.handlers;

import com.google.common.collect.Maps;
import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.network.NetworkHandler;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkDAO;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class NetworkCreateHandler {
    @Getter public final NetworkHandler handler;
    @Getter public final Map<UUID, Long> createCooldowns;

    public NetworkCreateHandler(NetworkHandler handler) {
        this.handler = handler;
        this.createCooldowns = Maps.newConcurrentMap();
    }

    /**
     * Create a new network
     * @param player Creator
     * @param name Network Name
     * @param promise Promise
     */
    public void createNetwork(Player player, String name, SimplePromise promise) {
        final UUID bukkitUUID = player.getUniqueId();
        final boolean admin = player.hasPermission("arescore.admin");

        if (createCooldowns.containsKey(player.getUniqueId()) && !admin) {
            final long remaining = createCooldowns.get(player.getUniqueId()) - Time.now();
            promise.fail("Please wait " + Time.convertToRemaining(remaining) + " before attempting to create another network");
            return;
        }

        if (!name.matches("^[A-Za-z0-9_.]+$")) {
            promise.fail("Name may only contain characters A-Z & 0-9");
            return;
        }

        if (name.length() < handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMinNetworkNameLength()) {
            promise.fail("Name must be at least " + handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMinNetworkNameLength() + " characters long");
            return;
        }

        if (name.length() > handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkNameLength()) {
            promise.fail("Name must be " + handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getMaxNetworkNameLength() + " characters long or less");
            return;
        }

        if (handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getBannedNetworkNames().contains(name.toUpperCase())) {
            promise.fail("This network name is not allowed");
            return;
        }

        if (handler.getManager().getNetworkByName(name) != null) {
            promise.fail("Network name is already in use");
            return;
        }

        final Network network = new Network(name, player);

        handler.getManager().getNetworkRepository().add(network);
        new Scheduler(handler.getManager().getPlugin()).async(() -> NetworkDAO.saveNetwork(handler.getManager().getPlugin().getDatabaseInstance(), network)).run();

        Logger.print(player.getName() + " created network " + name);

        createCooldowns.put(player.getUniqueId(), (Time.now() + (handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getNetworkCreateCooldown() * 1000L)));
        new Scheduler(handler.getManager().getPlugin()).sync(() -> createCooldowns.remove(bukkitUUID)).delay((handler.getManager().getPlugin().getConfigManager().getGeneralConfig().getNetworkCreateCooldown() * 20)).run();

        promise.success();
    }
}