package com.llewkcor.ares.core.alts.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.commons.util.general.Time;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public final class AltDAO {
    private static final String NAME = "ares";
    private static final String COLL = "acc_man";

    /**
     * Performs a scrub on the database to remove entries older than the provided expire time in seconds
     * @param database Database
     * @param expireSeconds Expire time in seconds
     */
    public static void cleanupExpiredAlts(MongoDB database, int expireSeconds) {
        final long expireTime = Time.now() - (expireSeconds * 1000);
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        collection.deleteMany(Filters.lt("last_seen", expireTime));
    }

    /**
     * Returns a Alt Entry matching the exact provided Bukkit UUID and converted IP Address
     * @param database Database
     * @param uniqueId Bukkit UUID
     * @param address Address
     * @return Alt Entry
     */
    public static AltEntry getAlt(MongoDB database, UUID uniqueId, long address) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.and(Filters.eq("id", uniqueId), Filters.eq("address", address))).first();

        if (existing == null) {
            return null;
        }

        return new AltEntry().fromDocument(existing);
    }

    /**
     * Returns an Immutable Collection containing all matching Alt Entries for the provided
     * Bukkit UUID and converted IP Address
     * @param database MongoDB Instance
     * @param uniqueId Bukkit UUID
     * @param address IP Address
     * @return Immutable Collection containing Alt Entries
     */
    public static ImmutableCollection<AltEntry> getAlts(MongoDB database, UUID uniqueId, long address) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final FindIterable<Document> iter = collection.find(Filters.or(Filters.eq("id", uniqueId), Filters.eq("address", address)));
        final List<AltEntry> result = Lists.newArrayList();

        for (Document document : iter) {
            result.add(new AltEntry().fromDocument(document));
        }

        return ImmutableList.copyOf(result);
    }

    /**
     * Saves the provided Alt Entry to the database
     * @param database Database
     * @param entry Alt Entry
     */
    public static void saveAlt(MongoDB database, AltEntry entry) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.and(Filters.eq("id", entry.getUniqueId()), Filters.eq("address", entry.getAddress()))).first();

        if (existing != null) {
            collection.replaceOne(existing, entry.toDocument());
        } else {
            collection.insertOne(entry.toDocument());
        }
    }

    /**
     * Deletes the provided Alt Entry from the database
     * @param database Database
     * @param entry Alt Entry
     */
    public static void deleteAlt(MongoDB database, AltEntry entry) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.and(Filters.eq("id", entry.getUniqueId()), Filters.eq("address", entry.getAddress()))).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}
