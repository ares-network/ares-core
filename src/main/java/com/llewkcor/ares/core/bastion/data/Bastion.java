package com.llewkcor.ares.core.bastion.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class Bastion implements MongoDocument<Bastion> {
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter public BLocatable location;
    @Getter @Setter public long matureTime;

    public Bastion() {
        this.uniqueId = null;
        this.ownerId = null;
        this.location = null;
        this.matureTime = 0L;
    }

    public Bastion(UUID ownerId, BLocatable location, long matureTime) {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.location = location;
        this.matureTime = matureTime;
    }

    /**
     * Returns true if the provided location is inside the provided radius no matter the Y-level
     * @param location Location
     * @param radius Radius
     * @return True if within radius
     */
    public boolean insideFlat(BLocatable location, double radius) {
        final BLocatable normalizedLocation = new BLocatable(location.getWorldName(), location.getX(), this.location.getY(), location.getZ());
        final double distance = this.location.distance(normalizedLocation);

        return (distance >= 0.0 && distance <= radius);
    }

    /**
     * Returns true if the provided location is inside the provided radius of this Bastion
     *
     * Will only return true if it meets game rules, which means a block directly underneath will not return true
     *
     * @param location Location
     * @param radius Radius
     * @return True if within radius
     */
    public boolean inside(BLocatable location, double radius) {
        if (location.getY() <= this.location.getY()) {
            return false;
        }

        final BLocatable normalizedLocation = new BLocatable(location.getWorldName(), location.getX(), this.location.getY(), location.getZ());
        final double distance = this.location.distance(normalizedLocation);

        return (distance >= 0.0 && distance <= radius);
    }

    /**
     * Returns true if this bastion is mature
     * @return True if mature
     */
    public boolean isMature() {
        return Time.now() >= matureTime;
    }

    @Override
    public Bastion fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.ownerId = (UUID)document.get("owner");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.matureTime = document.getLong("mature");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("owner", ownerId)
                .append("location", location.toDocument())
                .append("mature", matureTime);
    }
}