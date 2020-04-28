package com.playares.core.player.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.core.timers.data.PlayerTimer;
import com.playares.core.timers.data.type.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AresPlayer implements MongoDocument<AresPlayer> {
    @Getter public UUID uniqueId;
    @Getter @Setter public String username;
    @Getter @Setter public boolean spawned;
    @Getter @Setter public boolean resetOnJoin;
    @Getter public final Set<PlayerTimer> timers;
    @Getter public AresSettings settings;

    public AresPlayer() {
        this.uniqueId = null;
        this.username = null;
        this.spawned = false;
        this.resetOnJoin = true;
        this.timers = Sets.newConcurrentHashSet();
        this.settings = new AresSettings();
    }

    public AresPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.spawned = false;
        this.resetOnJoin = true;
        this.timers = Sets.newConcurrentHashSet();
        this.settings = new AresSettings();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AresPlayer fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("ares_id");
        this.spawned = document.getBoolean("spawned");
        this.resetOnJoin = document.getBoolean("reset_on_join");
        this.settings = new AresSettings().fromDocument(document.get("settings", Document.class));

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
                .append("reset_on_join", resetOnJoin)
                .append("spawned", spawned)
                .append("timers", timerDocuments)
                .append("settings", settings.toDocument());
    }

    public final class AresSettings implements MongoDocument<AresSettings> {
        @Getter @Setter public boolean autoAcceptNetworkInvites;
        @Getter @Setter public boolean snitchNotificationsEnabled;

        public AresSettings() {
            this.autoAcceptNetworkInvites = false;
            this.snitchNotificationsEnabled = true;
        }

        @SuppressWarnings("unchecked") @Override
        public AresSettings fromDocument(Document document) {
            this.autoAcceptNetworkInvites = document.getBoolean("auto_accept_network_invites");
            this.snitchNotificationsEnabled = document.getBoolean("snitch_notifications_enabled");

            return this;
        }

        @Override
        public Document toDocument() {
            return new Document()
                    .append("auto_accept_network_invites", autoAcceptNetworkInvites)
                    .append("snitch_notifications_enabled", snitchNotificationsEnabled);
        }
    }
}