package com.playares.core.player.listener;

import com.mongodb.client.model.Filters;
import com.playares.commons.logger.Logger;
import com.playares.core.player.PlayerManager;
import com.playares.core.player.data.AresPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@AllArgsConstructor
public final class PlayerListener implements Listener {
    @Getter public final PlayerManager manager;

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final AresPlayer existing = manager.getPlayerFromDatabase(Filters.eq("id", uniqueId));

        if (existing == null) {
            return;
        }

        manager.getPlayerRepository().add(existing);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        AresPlayer profile = manager.getPlayer(player.getUniqueId());

        if (profile == null) {
            profile = new AresPlayer(player);
            manager.getPlayerRepository().add(profile);
        } else if (profile.getUsername() == null || !profile.getUsername().equals(player.getName())) {
            profile.setUsername(player.getName());
            Logger.print("Updated Ares Player profile username for " + profile.getUsername());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlayer(player.getUniqueId());

        if (profile == null) {
            Logger.warn("Attempting to save a profile for " + player.getName() + " but it didn't exist");
            return;
        }

        manager.setPlayer(false, profile);
    }
}