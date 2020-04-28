package com.playares.core.network.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

public final class NetworkConfig implements MongoDocument<NetworkConfig> {
    @Getter @Setter public boolean passwordEnabled;
    @Getter @Setter public String password;
    @Getter @Setter public boolean snitchNotificationsEnabled;

    @Override
    public NetworkConfig fromDocument(Document document) {
        this.passwordEnabled = document.getBoolean("password_enabled");
        this.password = document.getString("password");
        this.snitchNotificationsEnabled = document.getBoolean("snitch_notifications_enabled");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("password_enabled", passwordEnabled)
                .append("password", password)
                .append("snitch_notifications_enabled", snitchNotificationsEnabled);
    }
}