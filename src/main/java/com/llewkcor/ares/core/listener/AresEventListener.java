package com.llewkcor.ares.core.listener;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.event.PlayerBigMoveEvent;
import com.llewkcor.ares.commons.event.PlayerDamagePlayerEvent;
import com.llewkcor.ares.commons.event.PlayerSplashPlayerEvent;
import com.llewkcor.ares.commons.event.ProcessedChatEvent;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

@AllArgsConstructor
public final class AresEventListener implements Listener {
    @Getter public final Ares plugin;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final ProcessedChatEvent aresEvent = new ProcessedChatEvent(event.getPlayer(), event.getMessage(), event.getRecipients());

        Bukkit.getPluginManager().callEvent(aresEvent);

        event.setCancelled(true);

        if (aresEvent.isCancelled()) {
            return;
        }

        aresEvent.getRecipients().forEach(viewer -> {
            if (viewer != null && viewer.isOnline()) {
                viewer.sendMessage(aresEvent.getDisplayName() + ": " + aresEvent.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final PlayerBigMoveEvent customEvent = new PlayerBigMoveEvent(player, from, to);
        Bukkit.getPluginManager().callEvent(customEvent);

        if (customEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player damaged = (Player)event.getEntity();
        Player damager = null;
        PlayerDamagePlayerEvent.DamageType type = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player)event.getDamager();
            type = PlayerDamagePlayerEvent.DamageType.PHYSICAL;
        }

        else if (event.getDamager() instanceof Projectile) {
            final Projectile projectile = (Projectile)event.getDamager();
            final ProjectileSource source = projectile.getShooter();

            if (!(source instanceof Player)) {
                return;
            }

            damager = (Player)source;
            type = PlayerDamagePlayerEvent.DamageType.PROJECTILE;
        }

        if (damager == null) {
            return;
        }

        final PlayerDamagePlayerEvent customEvent = new PlayerDamagePlayerEvent(damager, damaged, type);
        Bukkit.getPluginManager().callEvent(customEvent);

        if (customEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();

        if (!(potion.getShooter() instanceof Player)) {
            return;
        }

        if (event.getAffectedEntities().isEmpty()) {
            return;
        }

        final Player player = (Player)potion.getShooter();
        final List<LivingEntity> toRemove = Lists.newArrayList();

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player affected = (Player)entity;
            final PlayerSplashPlayerEvent customEvent = new PlayerSplashPlayerEvent(player, affected, potion);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                toRemove.add(affected);
            }
        }

        event.getAffectedEntities().removeAll(toRemove);
    }
}