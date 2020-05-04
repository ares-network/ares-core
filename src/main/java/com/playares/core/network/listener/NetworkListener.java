package com.playares.core.network.listener;

import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.network.NetworkManager;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
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
        final Collection<Network> invited = manager.getNetworksByInvite(player.getUniqueId());

        networks.forEach(network -> {
            final NetworkMember member = network.getMember(player);

            if (!member.getUsername().equals(player.getName())) {
                member.setUsername(player.getName());
            }

            network.setLastSeen(Time.now());
        });

        if (!invited.isEmpty()) {
            new Scheduler(manager.getPlugin()).sync(() -> {

                player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.YELLOW + invited.size() + ChatColor.AQUA + " pending network invitations");
                player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/network pending" + ChatColor.YELLOW + " to view all of them");

            }).delay(3 * 20L).run();
        }
    }
}