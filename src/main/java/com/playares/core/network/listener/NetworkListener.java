package com.playares.core.network.listener;

import com.playares.commons.util.general.Time;
import com.playares.core.network.NetworkManager;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
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

            network.setLastSeen(Time.now());
        });
    }
}