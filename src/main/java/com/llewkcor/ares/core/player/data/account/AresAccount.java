package com.llewkcor.ares.core.player.data.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.util.general.IPS;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AresAccount implements MongoDocument<AresAccount> {
    @Getter public UUID uniqueId;
    @Getter public UUID bukkitId;
    @Getter @Setter public String username;
    @Getter public long initialLogin;
    @Getter @Setter public long lastLogin;
    @Getter @Setter public boolean webConnected;
    @Getter @Setter public long address;
    @Getter @Setter public boolean resetOnJoin;
    @Getter @Setter public boolean spawned;
    @Getter public Set<PlayerTimer> timers;
    @Getter public AresAccountSettings settings;

    /**
     * Creates an empty Ares Account instance
     */
    public AresAccount() {
        this.uniqueId = UUID.randomUUID();
        this.bukkitId = null;
        this.username = null;
        this.initialLogin = Time.now();
        this.lastLogin = Time.now();
        this.webConnected = false;
        this.address = 0L;
        this.resetOnJoin = false;
        this.spawned = false;
        this.timers = Sets.newConcurrentHashSet();
        this.settings = new AresAccountSettings();
    }

    /**
     * Creates an Ares Account instance using provided information from a Bukkit Player instance
     * @param player Bukkit Player
     */
    public AresAccount(Player player) {
        this.uniqueId = UUID.randomUUID();
        this.bukkitId = player.getUniqueId();
        this.username = player.getName();
        this.initialLogin = Time.now();
        this.lastLogin = Time.now();
        this.webConnected = false;
        this.address = IPS.toLong(player.getAddress().getHostString());
        this.resetOnJoin = false;
        this.spawned = false;
        this.timers = Sets.newConcurrentHashSet();
        this.settings = new AresAccountSettings();
    }

    /**
     * Creates an Ares Account instance using provided Bukkit UUID and Bukkit Username information
     * @param bukkitId Bukkit UUID
     * @param username Bukkit Username
     */
    public AresAccount(UUID bukkitId, String username) {
        this.uniqueId = UUID.randomUUID();
        this.bukkitId = bukkitId;
        this.username = username;
        this.initialLogin = Time.now();
        this.lastLogin = Time.now();
        this.webConnected = false;
        this.address = 0L;
        this.resetOnJoin = false;
        this.spawned = false;
        this.timers = Sets.newConcurrentHashSet();
        this.settings = new AresAccountSettings();
    }

    @SuppressWarnings("unchecked") @Override
    public AresAccount fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("ares_id");
        this.bukkitId = (UUID)document.get("bukkit_id");
        this.username = document.getString("username");
        this.initialLogin = document.getLong("initial_login");
        this.lastLogin = document.getLong("last_login");
        this.webConnected = document.getBoolean("web_connected");
        this.address = document.getLong("address");
        this.resetOnJoin = document.getBoolean("reset_on_join");
        this.spawned = document.getBoolean("spawned");
        this.settings = new AresAccountSettings().fromDocument(document.get("settings", Document.class));

        final List<Document> timerDocuments = (List<Document>)document.get("timers", List.class);

        for (Document timerDocument : timerDocuments) {
            final PlayerTimerType type = PlayerTimerType.valueOf(timerDocument.getString("type"));

            if (type.equals(PlayerTimerType.ENDERPEARL)) {
                timers.add(new EnderpearlTimer().fromDocument(timerDocument));
                continue;
            }

            if (type.equals(PlayerTimerType.COMBAT)) {
                timers.add(new CombatTagTimer().fromDocument(timerDocument));
                continue;
            }

            if (type.equals(PlayerTimerType.CRAPPLE)) {
                timers.add(new CrappleTimer().fromDocument(timerDocument));
                continue;
            }

            if (type.equals(PlayerTimerType.GAPPLE)) {
                timers.add(new GappleTimer().fromDocument(timerDocument));
            }

            if (type.equals(PlayerTimerType.PEARL_PROTECTION)) {
                timers.add(new PearlProtectionTimer().fromDocument(timerDocument));
            }
        }

        return this;
    }

    @Override
    public Document toDocument() {
        final List<Document> timerDocuments = Lists.newArrayList();
        getTimers().forEach(timer -> timerDocuments.add(timer.toDocument()));

        return new Document()
                .append("ares_id", uniqueId)
                .append("bukkit_id", bukkitId)
                .append("username", username)
                .append("initial_login", initialLogin)
                .append("last_login", lastLogin)
                .append("web_connected", webConnected)
                .append("address", address)
                .append("reset_on_join", resetOnJoin)
                .append("spawned", spawned)
                .append("timers", timerDocuments)
                .append("settings", settings.toDocument());
    }

    public final class AresAccountSettings implements MongoDocument<AresAccountSettings> {
        @Getter @Setter public boolean privateMessagesEnabled;
        @Getter @Setter public boolean broadcastsEnabled;
        @Getter @Setter public boolean autoAcceptNetworkInvites;
        @Getter @Setter public boolean snitchNotificationsEnabled;
        @Getter @Setter public List<UUID> ignoredPlayers;

        public AresAccountSettings() {
            this.privateMessagesEnabled = true;
            this.broadcastsEnabled = true;
            this.autoAcceptNetworkInvites = false;
            this.snitchNotificationsEnabled = true;
            this.ignoredPlayers = Collections.synchronizedList(Lists.newArrayList());
        }

        /**
         * Returns true if this account is ignoring the provided Bukkit UUID
         * @param uniqueId Bukkit UUID
         * @return True if this player is ignoring the provided UUID
         */
        public boolean isIgnoring(UUID uniqueId) {
            return settings.ignoredPlayers.contains(uniqueId);
        }

        @SuppressWarnings("unchecked") @Override
        public AresAccountSettings fromDocument(Document document) {
            this.privateMessagesEnabled = document.getBoolean("private_messages_enabled");
            this.broadcastsEnabled = document.getBoolean("broadcasts_enabled");
            this.autoAcceptNetworkInvites = document.getBoolean("auto_accept_network_invites");
            this.snitchNotificationsEnabled = document.getBoolean("snitch_notifications_enabled");
            this.ignoredPlayers = (List<UUID>) document.get("ignored_players");

            return this;
        }

        @Override
        public Document toDocument() {
            return new Document()
                    .append("private_messages_enabled", privateMessagesEnabled)
                    .append("broadcasts_enabled", broadcastsEnabled)
                    .append("auto_accept_network_invites", autoAcceptNetworkInvites)
                    .append("snitch_notifications_enabled", snitchNotificationsEnabled)
                    .append("ignored_players", ignoredPlayers);
        }
    }
}