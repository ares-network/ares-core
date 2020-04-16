package com.llewkcor.ares.core.acid.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.network.data.Network;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.block.Block;

import java.util.UUID;

public final class AcidBlock implements MongoDocument<AcidBlock> {
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter public BLocatable location;
    @Getter @Setter public long matureTime;
    @Getter @Setter public long expireTime;

    public AcidBlock() {
        this.uniqueId = null;
        this.ownerId = null;
        this.location = null;
        this.matureTime = 0L;
        this.expireTime = 0L;
    }

    public AcidBlock(Network owner, Block block, long matureTime, long expireTime) {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = owner.getUniqueId();
        this.location = new BLocatable(block);
        this.matureTime = matureTime;
        this.expireTime = expireTime;
    }

    /**
     * Returns true if this Acid Block is mature
     * @return True if matured
     */
    public boolean isMature() {
        return this.matureTime <= Time.now();
    }

    /**
     * Returns true if this Acid Block is expired
     * @return True if expired
     */
    public boolean isExpired() {
        return this.expireTime <= Time.now();
    }

    /**
     * Returns true if the provided location is within the provided radius of this Acid Block
     * @param location Location
     * @param radius Radius
     * @return True if within radius
     */
    public boolean inside(BLocatable location, double radius) {
        final double distance = this.location.distance(location);
        return (distance >= 0.0 && distance <= radius);
    }

    @Override
    public AcidBlock fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.ownerId = (UUID)document.get("owner");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.matureTime = document.getLong("mature");
        this.expireTime = document.getLong("expire");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("owner", ownerId)
                .append("location", location.toDocument())
                .append("mature", matureTime)
                .append("expire", expireTime);
    }
}