package com.llewkcor.ares.core.loggers;

import com.google.common.collect.Sets;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.loggers.listener.LoggerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;

public final class LoggerManager {
    @Getter public final Ares plugin;
    @Getter public final Set<CombatLogger> activeLoggers;

    public LoggerManager(Ares plugin) {
        this.plugin = plugin;
        this.activeLoggers = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new LoggerListener(this), plugin);
    }

    /**
     * Returns a Combat Logger matching the provided Player UUID
     * @param uniqueId Player UUID
     * @return Combat Logger
     */
    public CombatLogger getLoggerByPlayer(UUID uniqueId) {
        return activeLoggers.stream().filter(logger -> logger.getOwnerId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Combat Logger matching the provided LivingEntity
     * @param entity LivingEntity
     * @return Combat Logger
     */
    public CombatLogger getLoggerByEntity(LivingEntity entity) {
        return activeLoggers.stream().filter(logger -> logger.getUniqueID().equals(entity.getUniqueId())).findFirst().orElse(null);
    }
}