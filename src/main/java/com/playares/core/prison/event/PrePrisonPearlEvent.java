package com.playares.core.prison.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public final class PrePrisonPearlEvent extends Event implements Cancellable {
    @Getter public UUID imprisoned;
    @Getter public Player killer;
    @Getter @Setter public int duration;
    @Getter @Setter public boolean cancelled;
    @Getter public static HandlerList handlerList = new HandlerList();

    public PrePrisonPearlEvent(UUID imprisoned, Player killer, int duration) {
        this.imprisoned = imprisoned;
        this.killer = killer;
        this.duration = duration;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
