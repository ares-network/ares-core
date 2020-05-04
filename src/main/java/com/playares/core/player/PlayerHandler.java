package com.playares.core.player;

import com.playares.commons.promise.SimplePromise;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.player.menu.AresSettingsMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class PlayerHandler {
    @Getter public final PlayerManager manager;

    /**
     * Handles opening the settings menu for the provided player
     * @param player Player
     * @param promise Promise
     */
    public void openSettingsMenu(Player player, SimplePromise promise) {
        final AresPlayer profile = manager.getPlayer(player.getUniqueId());

        if (profile == null) {
            promise.fail("Account not found");
            return;
        }

        final AresSettingsMenu menu = new AresSettingsMenu(manager.getPlugin(), player, profile.getSettings());
        menu.open();
    }
}