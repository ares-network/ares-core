package com.llewkcor.ares.core.snitch.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public final class SnitchEntry implements MongoDocument<SnitchEntry> {
    @Getter public UUID uniqueId;
    @Getter public SnitchEntryType type;
    @Getter public String entity;
    @Getter public String block;
    @Getter public String description;
    @Getter public BLocatable blockLocation;
    @Getter public long createdDate;
    @Getter public long expireDate;

    public SnitchEntry() {
        this.uniqueId = UUID.randomUUID();
        this.type = null;
        this.entity = null;
        this.block = null;
        this.description = "Something wonderful happened";
        this.blockLocation = null;
        this.createdDate = Time.now();
        this.expireDate = Time.now();
    }

    /**
     * Create a new Snitch instance with provided information
     * @param type Event Type
     * @param entityName Entity involved name
     * @param blockName Block involved name
     * @param description Description of the event
     * @param location Event location
     * @param expireDate Log expire date
     */
    public SnitchEntry(SnitchEntryType type, String entityName, String blockName, String description, BLocatable location, long expireDate) {
        this.uniqueId = UUID.randomUUID();
        this.type = type;
        this.entity = entityName;
        this.block = blockName;
        this.description = description;
        this.blockLocation = location;
        this.createdDate = Time.now();
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return "SnitchEntry {" +
                "uniqueId=" + uniqueId.toString() +
                ", type=" + type.name() +
                ", entity='" + entity + '\'' +
                ", block='" + block + '\'' +
                ", description='" + description + '\'' +
                ", blockLocation=" + blockLocation.toString() +
                ", createdDate=" + createdDate +
                ", expireDate=" + expireDate +
                '}';
    }

    @Override
    public SnitchEntry fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.type = SnitchEntryType.valueOf(document.getString("type"));
        this.entity = document.getString("entity_name");
        this.block = document.getString("block_name");
        this.description = document.getString("description");
        this.blockLocation = new BLocatable().fromDocument(document.get("block_location", Document.class));
        this.createdDate = document.getLong("created");
        this.expireDate = document.getLong("expire");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("type", type.name())
                .append("entity_name", entity)
                .append("block_name", block)
                .append("description", description)
                .append("block_location", blockLocation.toDocument())
                .append("created", createdDate)
                .append("expire", expireDate);
    }
}