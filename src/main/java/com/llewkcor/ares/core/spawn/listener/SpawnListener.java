package com.llewkcor.ares.core.spawn.listener;

import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.spawn.SpawnManager;
import com.llewkcor.ares.core.spawn.data.SpawnDAO;
import com.llewkcor.ares.core.spawn.data.SpawnData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

@AllArgsConstructor
public final class SpawnListener implements Listener {
    @Getter public final SpawnManager manager;

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final SpawnData spawnData = SpawnDAO.getSpawnData(manager.getPlugin().getDatabaseInstance(), uniqueId);

        if (spawnData != null) {
            manager.getSpawnData().add(spawnData);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());
        SpawnData spawnData = manager.getSpawnData(player);

        if (spawnData == null) {
            spawnData = new SpawnData(player);
            manager.getSpawnData().add(spawnData);

            final SpawnData finalizedSpawnData = spawnData;

            new Scheduler(manager.getPlugin()).async(() -> SpawnDAO.saveSpawnData(manager.getPlugin().getDatabaseInstance(), finalizedSpawnData)).run();
        }

        if (!spawnData.isSpawned() || spawnData.isSendToSpawnOnJoin() && prisonPearl == null) {
            spawnData.setSendToSpawnOnJoin(false);
            spawnData.setSpawned(false);

            new Scheduler(manager.getPlugin()).sync(() -> {
                player.teleport(manager.getSpawnLocation().getBukkit());
            }).delay(3L).run();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final SpawnData spawnData = manager.getSpawnData(player);
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());

        if (spawnData == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your spawn data");
            return;
        }

        if (prisonPearl != null) {
            return;
        }

        spawnData.setSpawned(false);
        spawnData.setSendToSpawnOnJoin(false);

        event.setRespawnLocation(manager.getSpawnLocation().getBukkit());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final SpawnData spawnData = manager.getSpawnData(player);

        manager.getTeleportRequests().remove(player.getUniqueId());

        if (spawnData == null) {
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            SpawnDAO.saveSpawnData(manager.getPlugin().getDatabaseInstance(), spawnData);
            manager.getSpawnData().remove(spawnData);
        }).run();
    }
}
