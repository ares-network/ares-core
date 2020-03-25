package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkDisplayHandler {
    @Getter public final NetworkHandler handler;

    public void printDisplay(Player player, String network, SimplePromise promise) {

    }
}