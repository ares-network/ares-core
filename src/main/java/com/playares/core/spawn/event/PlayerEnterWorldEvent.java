package com.playares.core.spawn.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerEnterWorldEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public PlayerEnterWorldMethod entranceMethod;
    @Getter @Setter public boolean cancelled;

    public PlayerEnterWorldEvent(Player who, PlayerEnterWorldMethod entranceMethod) {
        super(who);
        this.entranceMethod = entranceMethod;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum PlayerEnterWorldMethod {
        RANDOM, BED, REQUEST;
    }
}