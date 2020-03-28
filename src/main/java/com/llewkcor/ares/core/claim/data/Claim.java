package com.llewkcor.ares.core.claim.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class Claim implements MongoDocument<Claim> {
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter @Setter public BLocatable location;
    @Getter public ClaimType type;
    @Getter @Setter public int health;
    @Getter @Setter public long matureTime;

    public Claim() {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = null;
        this.location = null;
        this.type = null;
        this.health = 0;
        this.matureTime = 0L;
    }

    public Claim(UUID ownerId, BLocatable location, ClaimType type) {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.location = location;
        this.type = type;
        this.health = type.getDurability();
        this.matureTime = (Time.now() + (type.getMatureTimeInSeconds() * 1000L));
    }

    /**
     * Returns true if this Claim is matured
     * @return True if matured
     */
    public boolean isMatured() {
        return matureTime <= Time.now();
    }

    /**
     * Returns the percent display for the remaining health of this Claim
     * @return Health as percent
     */
    public String getHealthAsPercent() {
        return String.format("%.1f", (health * 100.0) / type.getDurability()) + "%";
    }

    @Override
    public Claim fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.ownerId = (UUID)document.get("owner_id");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.type = ClaimType.valueOf(document.getString("type"));
        this.health = document.getInteger("health");
        this.matureTime = document.getLong("mature");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("owner_id", ownerId)
                .append("location", location.toDocument())
                .append("type", type.name())
                .append("health", health)
                .append("mature", matureTime);
    }
}