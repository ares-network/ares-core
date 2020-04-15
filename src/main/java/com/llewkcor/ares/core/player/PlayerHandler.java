package com.llewkcor.ares.core.player;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.player.data.account.AccountDAO;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.player.menu.SettingsMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class PlayerHandler {
    @Getter public final PlayerManager manager;

    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Preparing to save " + manager.getAccountRepository().size() + " Accounts");
            manager.getAccountRepository().forEach(account -> AccountDAO.saveAccount(manager.getPlugin().getDatabaseInstance(), account));
            Logger.print("Finished saving " + manager.getAccountRepository().size() + " Accounts");
            return;
        }

        Logger.warn("Preparing to save " + manager.getAccountRepository().size() + " Accounts");
        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getAccountRepository().forEach(account -> AccountDAO.saveAccount(manager.getPlugin().getDatabaseInstance(), account));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Finished saving " + manager.getAccountRepository().size() + " Accounts")).run();
        }).run();
    }

    /**
     * Handles opening the settings menu
     * @param player Player
     * @param promise Promise
     */
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
