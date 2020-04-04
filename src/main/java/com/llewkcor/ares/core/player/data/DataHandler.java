package com.llewkcor.ares.core.player.data;

import com.llewkcor.ares.core.player.PlayerManager;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class DataHandler {
    @Getter public final PlayerManager playerManager;

    public DataHandler(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void createAccountCreateSession(Player player) {
        // TODO: createAccountCreateSession
    }

    public void createAccountResetSession(Player player) {
        // TODO: createAccountResetSession
    }
}