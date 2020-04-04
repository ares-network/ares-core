package com.llewkcor.ares.core.loggers.event;

import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerDamageLoggerEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final CombatLogger logger;
    @Getter @Setter public boolean cancelled;

    public PlayerDamageLoggerEvent(Player who, CombatLogger logger) {
        super(who);
        this.logger = logger;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}