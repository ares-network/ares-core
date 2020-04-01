package com.llewkcor.ares.core.network.listener;

import com.llewkcor.ares.core.network.NetworkManager;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

@AllArgsConstructor
public final class NetworkListener implements Listener {
    @Getter public final NetworkManager manager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Collection<Network> networks = manager.getNetworksByPlayer(player);

        networks.forEach(network -> {
            final NetworkMember member = network.getMember(player);

            if (!member.getUsername().equals(player.getName())) {
                member.setUsername(player.getName());
            }
        });
    }
}