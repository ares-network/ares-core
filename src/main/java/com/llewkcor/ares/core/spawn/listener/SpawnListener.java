package com.llewkcor.ares.core.spawn.listener;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.loggers.event.LoggerDeathEvent;
import com.llewkcor.ares.core.player.data.account.AccountDAO;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.spawn.SpawnManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public final class SpawnListener implements Listener {
    @Getter public final SpawnManager manager;

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

    @EventHandler
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

        if (!account.isSpawned() || account.isResetOnJoin() && prisonPearl == null) {
            account.setResetOnJoin(false);
            account.setSpawned(false);

            player.teleport(manager.getSpawnLocation().getBukkit());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
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
