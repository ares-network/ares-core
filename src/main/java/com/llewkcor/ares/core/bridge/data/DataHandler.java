package com.llewkcor.ares.core.bridge.data;

import lombok.Getter;
import org.bukkit.entity.Player;

public final class DataHandler {
    @Getter public final DataManager dataManager;

    public DataHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void createAccountCreateSession(Player player) {
        // TODO: createAccountCreateSession
    }

    public void createAccountResetSession(Player player) {
        // TODO: createAccountResetSession
    }
}