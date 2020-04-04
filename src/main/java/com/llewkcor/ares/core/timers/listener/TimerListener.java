package com.llewkcor.ares.core.timers.listener;

import com.llewkcor.ares.commons.event.PlayerDamagePlayerEvent;
import com.llewkcor.ares.commons.event.PlayerSplashPlayerEvent;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.loggers.event.PlayerDamageLoggerEvent;
import com.llewkcor.ares.core.timers.TimerManager;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class TimerListener implements Listener {
    @Getter public final TimerManager manager;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        final ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PlayerTimer existingTimer = manager.getTimer(player, PlayerTimerType.ENDERPEARL);

        if (existingTimer != null && !existingTimer.isExpired()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Enderpearls locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(existingTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
            return;
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof EnderPearl)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)projectile.getShooter();
        final PlayerTimer existingTimer = manager.getTimer(player, PlayerTimerType.ENDERPEARL);

        if (existingTimer != null && !existingTimer.isExpired()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Enderpearls locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(existingTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
            return;
        }

        manager.getHandler().addTimer(player, new EnderpearlTimer(player.getUniqueId(), 16));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        if (damaged.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }

        if (!manager.hasTimer(damaged, PlayerTimerType.COMBAT)) {
            damaged.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration() + " seconds");
        }

        if (!manager.hasTimer(damager, PlayerTimerType.COMBAT)) {
            damager.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        manager.getHandler().addTimer(damaged, new CombatTagTimer(damaged.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()));
        manager.getHandler().addTimer(damager, new CombatTagTimer(damager.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        if (damaged.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }

        if (!manager.hasTimer(damaged, PlayerTimerType.COMBAT)) {
            damaged.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration() + " seconds");
        }

        if (!manager.hasTimer(damager, PlayerTimerType.COMBAT)) {
            damager.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        manager.getHandler().addTimer(damaged, new CombatTagTimer(damaged.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()));
        manager.getHandler().addTimer(damager, new CombatTagTimer(damager.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (item.getType().equals(Material.GOLDEN_APPLE)) {
            if (item.getDurability() == (short)0) {
                final CrappleTimer crappleTimer = (CrappleTimer)manager.getTimer(player, PlayerTimerType.CRAPPLE);

                if (crappleTimer != null && !crappleTimer.isExpired()) {
                    player.sendMessage(ChatColor.RED + "Your crapples are locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(crappleTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
                    event.setCancelled(true);
                    return;
                }

                manager.getHandler().addTimer(player, new CrappleTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCrappleDuration()));

                return;
            }

            if (item.getDurability() == (short)1) {
                final GappleTimer gappleTimer = (GappleTimer)manager.getTimer(player, PlayerTimerType.GAPPLE);

                if (gappleTimer != null && !gappleTimer.isExpired()) {
                    player.sendMessage(ChatColor.RED + "Your crapples are locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(gappleTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
                    event.setCancelled(true);
                    return;
                }

                manager.getHandler().addTimer(player, new GappleTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getGappleDuration()));
            }
        }
    }

    @EventHandler
    public void onLoggerDamage(PlayerDamageLoggerEvent event) {
        final Player player = event.getPlayer();

        if (!manager.hasTimer(player, PlayerTimerType.COMBAT)) {
            player.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        manager.getHandler().addTimer(player, new CombatTagTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }
}