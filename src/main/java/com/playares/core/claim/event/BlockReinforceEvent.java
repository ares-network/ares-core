package com.playares.core.claim.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public final class BlockReinforceEvent extends Event implements Cancellable {
    @Getter public Player player;
    @Getter public List<Block> blocks;
    @Getter @Setter public boolean cancelled;
    @Getter public static HandlerList handlerList = new HandlerList();

    public BlockReinforceEvent(Player player, List<Block> blocks) {
        this.player = player;
        this.blocks = blocks;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}