package com.llewkcor.ares.core.network.handlers;

import com.google.common.collect.Maps;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class NetworkManageHandler {
    @Getter public final NetworkHandler handler;
    @Getter public final Map<UUID, Long> renameCooldowns;

    public NetworkManageHandler(NetworkHandler handler) {
        this.handler = handler;
        this.renameCooldowns = Maps.newConcurrentMap();
    }

    public void deleteNetwork(Player player, String network, SimplePromise promise) {

    }

    public void leaveNetwork(Player player, String network, SimplePromise promise) {

    }

    public void kickFromNetwork(Player player, String network, String username, SimplePromise promise) {

    }

    public void renameNetwork(Player player, String network, String newName, SimplePromise promise) {

    }
}
