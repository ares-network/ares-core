package com.llewkcor.ares.core.snitch.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.network.data.NetworkMember;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public final class Snitch implements MongoDocument<Snitch> {
    @Getter public UUID uniqueId;
    @Getter public String name;
    @Getter public UUID ownerId;
    @Getter public BLocatable location;
    @Getter @Setter public long matureTime;
    @Getter public Set<SnitchEntry> logEntries;
    @Getter public Map<UUID, BLocatable> spotted;

    public Snitch() {
        this.uniqueId = UUID.randomUUID();
        this.name = "Generic Snitch";
        this.ownerId = null;
        this.location = null;
        this.matureTime = Time.now();
        this.logEntries = Sets.newConcurrentHashSet();
        this.spotted = Maps.newConcurrentMap();
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
        this.logEntries = Sets.newConcurrentHashSet();
        this.spotted = Maps.newConcurrentMap();
    }

    /**
     * Returns true if this snitch is matured
     * @return True if matured
     */
    public boolean isMature() {
        return matureTime <= Time.now();
    }

    /**
     * Returns true if player is spotted
     * @param player Bukkit Player
     * @return True if spotted
     */
    public boolean isSpotted(Player player) {
        return spotted.containsKey(player.getUniqueId());
    }

    /**
     * Returns true if the provided Bukkit UUID is spotted
     * @param uniqueId Bukkit UUID
     * @return True if spotted
     */
    public boolean isSpotted(UUID uniqueId) {
        return spotted.containsKey(uniqueId);
    }

    /**
     * Returns true if the player has moved since they were last spotted
     * @param player Player
     * @return True if the player has moved
     */
    public boolean hasMovedSinceLastSeen(Player player) {
        if (!isSpotted(player)) {
            return true;
        }

        final BLocatable location = new BLocatable(player.getLocation().getBlock());
        final BLocatable lastSeenLocation = spotted.get(player.getUniqueId());

        return (location.getX() == lastSeenLocation.getX() && location.getY() == lastSeenLocation.getY() && location.getZ() == lastSeenLocation.getZ());
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

    @SuppressWarnings("unchecked") @Override
    public Snitch fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.ownerId = (UUID)document.get("owner_id");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.matureTime = document.getLong("mature_time");
        this.logEntries = Sets.newConcurrentHashSet();

        final List<Document> logEntryDocuments = document.get("log_entries", List.class);
        logEntryDocuments.forEach(logEntryDocument -> logEntries.add(new SnitchEntry().fromDocument(logEntryDocument)));

        return this;
    }

    @Override
    public Document toDocument() {
        final List<Document> logEntryDocuments = Lists.newArrayList();
        logEntries.forEach(entry -> logEntryDocuments.add(entry.toDocument()));

        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("owner_id", ownerId)
                .append("location", location.toDocument())
                .append("mature_time", matureTime)
                .append("log_entries", logEntryDocuments);
    }
}