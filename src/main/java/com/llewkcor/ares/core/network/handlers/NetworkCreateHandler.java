package com.llewkcor.ares.core.network.handlers;

import com.google.common.collect.Maps;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
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

    public void createNetwork(Player player, String name, SimplePromise promise) {

    }
}
