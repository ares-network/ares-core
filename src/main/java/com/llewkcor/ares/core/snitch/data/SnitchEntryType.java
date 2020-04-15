package com.llewkcor.ares.core.snitch.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SnitchEntryType {
    BLOCK_BREAK("Block Break", "broke a block"),
    BLOCK_PLACE("Block Place", "placed a block"),
    BLOCK_INTERACTION("Block Interaction", "interacted with a block"),
    LOGIN("Connected", "connected to the server"),
    LOGOUT("Disconnected", "disconnected from the server"),
    SPOTTED("Spotted", "was spotted"),
    KILL("Player Slain", "was slain");

    @Getter public final String displayName;
    @Getter public final String descriptor;
}