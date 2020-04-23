package com.llewkcor.ares.core.spawn.listener;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.util.bukkit.Players;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.loggers.event.LoggerDeathEvent;
import com.llewkcor.ares.core.player.data.account.AccountDAO;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.spawn.SpawnManager;
import com.llewkcor.ares.core.spawn.event.PlayerEnterWorldEvent;
import com.llewkcor.ares.core.spawn.kits.data.SpawnKit;
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
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null || account.isSpawned()) {
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
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null || account.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onVelocity(PlayerVelocityEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null || account.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null || account.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null || account.isSpawned()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            Logger.error("Failed to obtain account for " + player.getName());
            return;
        }

        if ((!account.isSpawned() || account.isResetOnJoin() || !player.hasPlayedBefore()) && prisonPearl == null) {
            account.setResetOnJoin(false);
            account.setSpawned(false);

            player.teleport(manager.getSpawnLocation().getBukkit());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            Players.resetHealth(player);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());

        if (account == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your account");
            return;
        }

        if (prisonPearl != null) {
            return;
        }

        account.setSpawned(false);
        account.setResetOnJoin(false);

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

        manager.getPlugin().getPlayerManager().getAccountByBukkitID(logger.getOwnerId(), new FailablePromise<AresAccount>() {
            @Override
            public void success(AresAccount aresAccount) {
                if (aresAccount == null) {
                    Logger.print("Failed to update resetOnJoin for " + logger.getOwnerUsername() + ", Reason: AresAccount null");
                    return;
                }

                aresAccount.setResetOnJoin(true);

                new Scheduler(manager.getPlugin()).async(() -> AccountDAO.saveAccount(manager.getPlugin().getDatabaseInstance(), aresAccount)).run();
            }

            @Override
            public void fail(String s) {
                Logger.print("Failed to update resetOnJoin for " + logger.getOwnerUsername() + ", Reason: " + s);
            }
        });
    }
}
