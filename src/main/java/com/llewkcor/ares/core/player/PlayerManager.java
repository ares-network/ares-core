package com.llewkcor.ares.core.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.player.data.DataHandler;
import com.llewkcor.ares.core.player.data.account.AccountDAO;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.player.data.listener.AccountListener;
import com.llewkcor.ares.core.player.data.session.AccountCreateSession;
import com.llewkcor.ares.core.player.data.session.AccountResetSession;
import com.llewkcor.ares.core.player.data.session.AccountSession;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager {
    @Getter public final Ares plugin;
    @Getter public final PlayerHandler handler;
    @Getter public final DataHandler dataHandler;
    @Getter public final Set<AresAccount> accountRepository;
    @Getter public final Set<AccountSession> accountSessionRepository;

    public PlayerManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new PlayerHandler(this);
        this.dataHandler = new DataHandler(this);
        this.accountRepository = Sets.newConcurrentHashSet();
        this.accountSessionRepository = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new AccountListener(this), plugin);
    }

    /**
     * Retrieves an Ares Account from cache only using the Ares Account ID
     * @param uniqueId Ares Account ID
     * @return Ares Account
     */
    public AresAccount getAccountByAresID(UUID uniqueId) {
        return accountRepository.stream().filter(account -> account.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Retrieves an Ares Account from cache only using the Bukkit UUID
     * @param uniqueId Bukkit UUID
     * @return Ares Account
     */
    public AresAccount getAccountByBukkitID(UUID uniqueId) {
        return accountRepository.stream().filter(account -> account.getBukkitId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Retrieves an Ares Account by Ares Account ID
     * @param uniqueId Ares Account UUID
     * @param promise Promise
     */
    public void getAccountByAresID(UUID uniqueId, FailablePromise<AresAccount> promise) {
        final AresAccount cachedProfile = accountRepository.stream().filter(account -> account.getUniqueId().equals(uniqueId)).findFirst().orElse(null);

        if (cachedProfile != null) {
            promise.success(cachedProfile);
            return;
        }

        new Scheduler(plugin).async(() -> {
            final AresAccount result = AccountDAO.getAccountByAresID(plugin.getDatabaseInstance(), uniqueId);

            new Scheduler(plugin).sync(() -> {
                if (result != null) {
                    promise.success(result);
                    return;
                }

                promise.fail("Account not found");
            }).run();
        }).run();
    }

    /**
     * Retrieves an Ares Account by Bukkit Account ID
     * @param uniqueId Bukkit Account UUID
     * @param promise Promise
     */
    public void getAccountByBukkitID(UUID uniqueId, FailablePromise<AresAccount> promise) {
        final AresAccount cachedProfile = accountRepository.stream().filter(account -> account.getBukkitId().equals(uniqueId)).findFirst().orElse(null);

        if (cachedProfile != null) {
            promise.success(cachedProfile);
            return;
        }

        new Scheduler(plugin).async(() -> {
            final AresAccount result = AccountDAO.getAccountByBukkitID(plugin.getDatabaseInstance(), uniqueId);

            new Scheduler(plugin).sync(() -> {
                if (result != null) {
                    promise.success(result);
                    return;
                }

                promise.fail("Account not found");
            }).run();
        }).run();
    }

    /**
     * Retrieves an Ares Account by Bukkit Username
     * @param username Username
     * @param promise Promise
     */
    public void getAccountByUsername(String username, FailablePromise<AresAccount> promise) {
        final AresAccount cachedProfile = accountRepository.stream().filter(account -> account.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);

        if (cachedProfile != null) {
            promise.success(cachedProfile);
            return;
        }

        new Scheduler(plugin).async(() -> {
            final AresAccount result = AccountDAO.getAccountByUsername(plugin.getDatabaseInstance(), username);

            new Scheduler(plugin).sync(() -> {
                if (result != null) {
                    promise.success(result);
                    return;
                }

                promise.fail("Account not found");
            }).run();
        }).run();
    }

    /**
     * Returns a collection of expired Account Sessions
     * @return Collection of expired account sessions
     */
    public ImmutableList<AccountSession> getExpiredSessions() {
        return ImmutableList.copyOf(accountSessionRepository.stream().filter(AccountSession::isExpired).collect(Collectors.toList()));
    }

    /**
     * Returns an Account Create Session matching the provided Ares UUID
     * @param aresId Ares UUID
     * @return Account Create Session
     */
    public AccountCreateSession getAccountCreateSessionByAresID(UUID aresId) {
        return (AccountCreateSession)accountSessionRepository
                .stream()
                .filter(session -> (session instanceof AccountCreateSession))
                .filter(createSession -> createSession.getUniqueId().equals(aresId))
                .filter(foundSession -> (!foundSession.isExpired()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns an Account Reset Session matching the provided Ares UUID
     * @param aresId Ares UUID
     * @return Account Reset Session
     */
    public AccountResetSession getAccountResetSessionByAresID(UUID aresId) {
        return (AccountResetSession) accountSessionRepository
                .stream()
                .filter(session -> (session instanceof AccountResetSession))
                .filter(resetSession -> resetSession.getUniqueId().equals(aresId))
                .filter(foundSession -> (!foundSession.isExpired()))
                .findFirst()
                .orElse(null);
    }
}