package com.llewkcor.ares.core.prison.event;

import com.llewkcor.ares.core.prison.data.PrisonPearl;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PrisonPearlCreateEvent extends Event {
    @Getter public PrisonPearl prisonPearl;
    @Getter public static HandlerList handlerList = new HandlerList();

    public PrisonPearlCreateEvent(PrisonPearl prisonPearl) {
        this.prisonPearl = prisonPearl;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
