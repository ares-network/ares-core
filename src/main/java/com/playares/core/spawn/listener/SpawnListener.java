package com.playares.core.spawn.listener;

import com.mongodb.client.model.Filters;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Players;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.loggers.entity.CombatLogger;
import com.playares.core.loggers.event.LoggerDeathEvent;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.prison.data.PrisonPearl;
import com.playares.core.spawn.SpawnManager;
import com.playares.core.spawn.event.PlayerEnterWorldEvent;
import com.playares.core.spawn.kits.data.SpawnKit;
import com.playares.luxe.rewards.event.PlayerClaimRewardEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import java.util.UUID;

@AllArgsConstructor
public final class SpawnListener implements Listener {
    @Getter public final SpawnManager manager;

    @EventHandler
    public void onRewardClaim(PlayerClaimRewardEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        if (!profile.isSpawned()) {
            player.sendMessage(ChatColor.RED + "Rewards can not be claimed while you are in Spawn");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerEnterWorld(PlayerEnterWorldEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final SpawnKit kit = manager.getKitManager().getKit(player);

        if (manager.getKitManager().hasKitCooldown(player)) {
            final SpawnKit defaultKit = manager.getKitManager().getDefaultKit();

            player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.RED + "" + ChatColor.BOLD + (Time.convertToDecimal(manager.getKitManager().getKitCooldown(player) - Time.now())) + ChatColor.RED + "s before obtaining your starter kit again");
            defaultKit.give(player);

            return;
        }

        if (kit.isDefault()) {
            return;
        }

        kit.give(player);
        manager.getKitManager().getKitCooldowns().put(player.getUniqueId(), (Time.now() + (manager.getKitManager().getSpawnKitObtainCooldown() * 1000L)));

        new Scheduler(manager.getPlugin()).sync(() -> {
            manager.getKitManager().getKitCooldowns().remove(uniqueId);

            if (Bukkit.getPlayer(uniqueId) != null) {
                final Player futurePlayer = Bukkit.getPlayer(uniqueId);

                futurePlayer.sendMessage(ChatColor.GREEN + "Your spawn kit can now be used again");
                Players.playSound(futurePlayer, Sound.NOTE_BASS_GUITAR);
            }
        }).delay(manager.getKitManager().getSpawnKitObtainCooldown() * 20).run();
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        final Player player = (Player)event.getEntity();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.isSpawned()) {
            return;
        }

        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onVelocity(PlayerVelocityEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            Logger.error("Failed to obtain account for " + player.getName());
            return;
        }

        if ((!profile.isSpawned() || profile.isResetOnJoin() || !player.hasPlayedBefore()) && prisonPearl == null) {
            profile.setResetOnJoin(false);
            profile.setSpawned(false);

            player.teleport(manager.getSpawnLocation().getBukkit());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            Players.resetHealth(player);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your account");
            return;
        }

        if (prisonPearl != null) {
            return;
        }

        profile.setSpawned(false);
        profile.setResetOnJoin(false);

        event.setRespawnLocation(manager.getSpawnLocation().getBukkit());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        manager.getTeleportRequests().remove(player.getUniqueId());
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();

        new Scheduler(manager.getPlugin()).async(() -> {
            final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayerFromDatabase(Filters.eq("id", logger.getOwnerId()));

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (profile == null) {
                    Logger.print("Failed to update resetOnJoin for " + logger.getOwnerUsername() + ", Reason: AresAccount null");
                    return;
                }

                profile.setResetOnJoin(true);
                manager.getPlugin().getPlayerManager().setPlayer(false, profile);
            }).run();
        }).run();
    }
}
