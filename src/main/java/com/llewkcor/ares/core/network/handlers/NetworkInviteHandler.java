package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.network.NetworkHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkInviteHandler {
    @Getter public final NetworkHandler handler;

    public void inviteMember(Player player, String network, String username, SimplePromise promise) {

    }

    public void uninviteMember(Player player, String network, String username, SimplePromise promise) {

    }

    public void acceptInvite(Player player, String network, SimplePromise promise) {

    }
}
