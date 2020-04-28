package com.playares.core.alts.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class AltEntry implements MongoDocument<AltEntry> {
    @Getter public UUID uniqueId;
    @Getter public long address;
    @Getter public long create;
    @Getter @Setter public long lastSeen;

    public AltEntry() {
        this.uniqueId = null;
        this.address = 0;
        this.create = Time.now();
        this.lastSeen = Time.now();
    }

    /**
     * Create an new Alt Entry instance
     * @param uniqueId Bukkit UUID
     * @param address Converted IP Address
     */
    public AltEntry(UUID uniqueId, long address) {
        this.uniqueId = uniqueId;
        this.address = address;
        this.create = Time.now();
        this.lastSeen = Time.now();
    }

    @Override
    public AltEntry fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.address = document.getLong("address");
        this.create = document.getLong("create");
        this.lastSeen = document.getLong("last_seen");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("address", address)
                .append("create", create)
                .append("last_seen", lastSeen);
    }
}
