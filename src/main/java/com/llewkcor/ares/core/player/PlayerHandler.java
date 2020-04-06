package com.llewkcor.ares.core.player;

import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.player.menu.SettingsMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class PlayerHandler {
    @Getter public final PlayerManager manager;

    public void openSettings(Player player, SimplePromise promise) {
        final AresAccount account = manager.getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        final SettingsMenu menu = new SettingsMenu(manager.getPlugin(), player, account);
        menu.open();
        promise.success();
    }
}
