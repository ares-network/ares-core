package com.llewkcor.ares.core.timers.event;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.List;

public final class HUDUpdateEvent extends PlayerEvent {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter List<String> entries;

    public HUDUpdateEvent(Player who) {
        super(who);
        this.entries = Lists.newArrayList();
    }

    public void add(String entry) {
        entries.add(entry);
    }

    public void clear() {
        entries.clear();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
