package com.llewkcor.ares.core.claim.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public final class BlockReinforceEvent extends BlockEvent implements Cancellable {
    @Getter public Player player;
    @Getter @Setter public boolean cancelled;
    @Getter public static HandlerList handlerList = new HandlerList();

    public BlockReinforceEvent(Player player, Block block) {
        super(block);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}