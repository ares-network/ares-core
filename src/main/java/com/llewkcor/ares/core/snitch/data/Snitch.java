package com.llewkcor.ares.core.snitch.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.block.Block;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Snitch implements MongoDocument<Snitch> {
    @Getter public UUID uniqueId;
    @Getter public String name;
    @Getter public UUID ownerId;
    @Getter public BLocatable location;
    @Getter @Setter public long matureTime;
    @Getter public Set<SnitchEntry> logEntries;

    public Snitch() {
        this.uniqueId = UUID.randomUUID();
        this.name = "Generic Snitch";
        this.ownerId = null;
        this.location = null;
        this.matureTime = Time.now();
        this.logEntries = Sets.newConcurrentHashSet();
    }

    /**
     * Create a snitch with the provided information
     * @param name Snitch name
     * @param ownerId Snitch Owner ID
     * @param block Snitch Bukkit Block
     * @param matureTime Snitch mature time
     */
    public Snitch(String name, UUID ownerId, Block block, long matureTime) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.ownerId = ownerId;
        this.location = new BLocatable(block);
        this.matureTime = matureTime;
    }

    /**
     * Returns a sorted immutable list of Snitch Entries by creation date
     * @return ImmutableList of SnitchEntry instances
     */
    public ImmutableList<SnitchEntry> getSortedEntries() {
        final List<SnitchEntry> entries = Lists.newArrayList(logEntries);
        entries.sort(Comparator.comparingLong(SnitchEntry::getCreatedDate));
        return ImmutableList.copyOf(entries);
    }

    /**
     * Returns true if the provided block location is within the provided radius of this snitch
     * @param location Block location
     * @param radius Radius
     * @return True if within radius
     */
    public boolean inRadius(BLocatable location, double radius) {
        return this.location.distance(location) <= radius;
    }

    @Override
    public Snitch fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.ownerId = (UUID)document.get("owner_id");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.matureTime = document.getLong("mature_time");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("owner_id", ownerId)
                .append("location", location.toDocument())
                .append("mature_time", matureTime);
    }
}