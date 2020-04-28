package com.playares.core.chat.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public final class ChatSession {
    @Getter public final UUID playerId;
    @Getter public final UUID networkId;
}
