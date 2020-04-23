package com.llewkcor.ares.core.loggers.listener;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.chat.data.ChatMessageType;
import com.llewkcor.ares.core.loggers.LoggerManager;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.loggers.event.LoggerDeathEvent;
import com.llewkcor.ares.core.loggers.event.PlayerDamageLoggerEvent;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import com.llewkcor.ares.core.utils.PlayerUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.projectiles.ProjectileSource;

@AllArgsConstructor
public final class LoggerListener implements Listener {
    @Getter public final LoggerManager manager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final CombatLogger logger = manager.getLoggerByPlayer(player.getUniqueId());

        if (logger == null) {
            return;
        }

        logger.reapply(player);
        logger.getBukkitEntity().remove();
        manager.getActiveLoggers().remove(logger);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final double radius = manager.getPlugin().getConfigManager().getGeneralConfig().getCombatLoggerEnemyRadius();
        boolean combatLog = false;

        if (account == null || !account.isSpawned() || player.hasPermission("arescore.admin")) {
            return;
        }

        if (manager.getPlugin().getTimerManager().hasTimer(player, PlayerTimerType.COMBAT)) {
            combatLog = true;
        }

        if (player.getNoDamageTicks() > 0) {
            combatLog = true;
        }

        if (player.getFallDistance() >= 4.0) {
            combatLog = true;
        }

        if (PlayerUtil.isNearbyEnemy(manager.getPlugin(), player.getNearbyEntities(radius, radius, radius), player)) {
            combatLog = true;
        }

        if (!combatLog) {
            Logger.print("C");
            return;
        }

        Logger.print("D");

        manager.getPlugin().getChatManager().getRecipientsInRange(player.getLocation(), ChatMessageType.COMBAT_LOGGED).forEach(notified -> notified.sendMessage(ChatColor.RED + "Combat-Logger: " + ChatColor.BLUE + player.getName()));

        final CombatLogger logger = new CombatLogger(
                ((CraftWorld) player.getWorld()).getHandle(),
                player,
                manager.getPlugin().getPrisonPearlManager().getBanDuration(player),
                manager.getPlugin().getTimerManager().hasTimer(player, PlayerTimerType.PEARL_PROTECTION));

        logger.spawn();
        manager.getActiveLoggers().add(logger);

        new Scheduler(manager.getPlugin()).sync(() -> {
            if (manager.getActiveLoggers().contains(logger)) {

                logger.getBukkitEntity().remove();
                manager.getActiveLoggers().remove(logger);

            }
        }).delay(manager.getPlugin().getConfigManager().getGeneralConfig().getCombatLoggerDuration() * 20).run();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final EntityLiving nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();

        if (!(nmsEntity instanceof CombatLogger)) {
            return;
        }

        final CombatLogger logger = (CombatLogger)nmsEntity;

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        final Entity damager = event.getDamager();

        if (damager instanceof Projectile) {
            final ProjectileSource source = ((Projectile) damager).getShooter();

            if (source instanceof Player) {
                final Player playerDamager = (Player)source;
                final PlayerDamageLoggerEvent loggerEvent = new PlayerDamageLoggerEvent(playerDamager, logger);

                Bukkit.getPluginManager().callEvent(loggerEvent);

                if (loggerEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            return;
        }

        if (damager instanceof Player) {
            final Player playerDamager = (Player)damager;

            final PlayerDamageLoggerEvent loggerEvent = new PlayerDamageLoggerEvent(playerDamager, logger);

            Bukkit.getPluginManager().callEvent(loggerEvent);

            if (loggerEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final EntityLiving nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();

        if (!(nmsEntity instanceof CombatLogger)) {
            return;
        }

        final CombatLogger logger = (CombatLogger)nmsEntity;

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        final LoggerDeathEvent deathEvent = new LoggerDeathEvent(logger, event.getEntity().getKiller());

        Bukkit.getPluginManager().callEvent(deathEvent);

        logger.dropItems(event.getEntity().getLocation());
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();

        if (event.isCancelled()) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        final LivingEntity livingEntity = (LivingEntity)entity;
        final EntityLiving asNms = ((CraftLivingEntity)livingEntity).getHandle();

        if (!(asNms instanceof CombatLogger)) {
            return;
        }

        final CombatLogger logger = (CombatLogger)asNms;

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Chunk chunk = event.getChunk();

        if (manager.getActiveLoggers().stream().anyMatch(logger -> logger.getBukkitEntity().getLocation().getChunk().getX() == chunk.getX() && logger.getBukkitEntity().getLocation().getChunk().getZ() == chunk.getZ())) {
            event.setCancelled(true);
        }
    }
}