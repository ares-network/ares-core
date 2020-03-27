package com.llewkcor.ares.core.snitch.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SnitchEntryType {
    BLOCK_BREAK("Block Break"),
    BLOCK_PLACE("Block Place"),
    BLOCK_INTERACTION("Block Interaction"),
    LOGIN("Connected"),
    LOGOUT("Disconnected"),
    SPOTTED("Spotted"),
    KILL("Player Slain");

    @Getter public final String displayName;
}
