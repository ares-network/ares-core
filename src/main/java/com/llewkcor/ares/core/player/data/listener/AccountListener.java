package com.llewkcor.ares.core.player.data.listener;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.event.ProcessedChatEvent;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.util.general.IPS;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.player.PlayerManager;
import com.llewkcor.ares.core.player.data.account.AccountDAO;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

public final class AccountListener implements Listener {
    @Getter public final PlayerManager playerManager;

    public AccountListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    /**
     * Handles Ares Account loading/creation
     * @param event Bukkit Event
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!playerManager.getPlugin().getDatabaseInstance().isConnected()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server is still starting up");
            return;
        }

        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final String address = event.getAddress().getHostAddress();
        final long convertedAddress = IPS.toLong(address);
        boolean updated = false;
        AresAccount account = AccountDAO.getAccountByBukkitID(playerManager.getPlugin().getDatabaseInstance(), uniqueId);

        if (account == null) {
            account = new AresAccount(uniqueId, username);
            updated = true;
        }

        if (!account.getUsername().equals(username)) {
            Logger.print("Updated profile username " + account.getUsername() + " to " + username);
            account.setUsername(username);
            updated = true;
        }

        if (convertedAddress != account.getAddress()) {
            Logger.print("Updated profile address for " + username);
            account.setAddress(convertedAddress);
            updated = true;
        }

        if (updated) {
            AccountDAO.saveAccount(playerManager.getPlugin().getDatabaseInstance(), account);
        }

        playerManager.getAccountRepository().add(account);
    }

    /**
     * Handles issuing warning if player is not connected to the web
     * @param event Bukkit PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = playerManager.getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            return;
        }

        if (!account.isWebConnected()) {
            player.sendMessage(ChatColor.RED + "Your account is not verified. Type '/account create' to get started.");
        }
    }

    /**
     * Handles saving Ares Account to database upon disconnecting
     * @param event Bukkit PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        playerManager.getAccountByBukkitID(player.getUniqueId(), new FailablePromise<AresAccount>() {
            @Override
            public void success(AresAccount aresAccount) {
                aresAccount.setLastLogin(Time.now());

                AccountDAO.saveAccount(playerManager.getPlugin().getDatabaseInstance(), aresAccount);
                playerManager.getAccountRepository().remove(aresAccount);
            }

            @Override
            public void fail(String s) {
                Logger.warn("Attempted to save Ares Account upon disconnect for " + player.getName() + " but was unable to obtain it. Reason: " + s);
            }
        });
    }

    /**
     * Handles filtering chat messages and applies each players settings
     * @param event Ares ProcessedChatEvent
     */
    @EventHandler (priority = EventPriority.LOW)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final List<Player> toRemove = Lists.newArrayList();

        if (account == null) {
            return;
        }

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        for (Player recipient : event.getRecipients()) {
            final AresAccount viewerAccount = getPlayerManager().getAccountByBukkitID(recipient.getUniqueId());

            if (account.getSettings().isIgnoring(recipient.getUniqueId()) || viewerAccount.getSettings().isIgnoring(player.getUniqueId())) {
                toRemove.add(recipient);
            }
        }

        event.getRecipients().removeAll(toRemove);
    }
}