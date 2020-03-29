package com.llewkcor.ares.core.prison.event;

import com.llewkcor.ares.core.prison.data.PrisonPearl;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PrisonPearlReleaseEvent extends Event {
    @Getter public PrisonPearl prisonPearl;
    @Getter public String reason;
    @Getter public static HandlerList handlerList = new HandlerList();

    public PrisonPearlReleaseEvent(PrisonPearl prisonPearl, String reason) {
        this.prisonPearl = prisonPearl;
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}