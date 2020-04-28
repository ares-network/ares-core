package com.playares.core.timers.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.timer.Timer;
import com.playares.core.timers.data.type.PlayerTimerType;
import lombok.Getter;

import java.util.UUID;

public abstract class PlayerTimer extends Timer implements MongoDocument<Timer> {
    @Getter public UUID owner;
    @Getter public PlayerTimerType type;

    public PlayerTimer(UUID owner, PlayerTimerType type, int seconds) {
        super(seconds);
        this.owner = owner;
        this.type = type;
    }

    public PlayerTimer(UUID owner, PlayerTimerType type, long milliseconds) {
        super(milliseconds);
        this.owner = owner;
        this.type = type;
    }
}
