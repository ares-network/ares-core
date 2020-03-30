package com.llewkcor.ares.core.spawn.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.UUID;

public final class SpawnDAO {
    private static final String NAME = "ares";
    private static final String COLL = "spawn_data";

    /**
     * Returns a SpawnData object for the provided Bukkit UUID
     * @param database MongoDB Database
     * @param uniqueId Bukkit UUID
     * @return SpawnData
     */
    public static SpawnData getSpawnData(MongoDB database, UUID uniqueId) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", uniqueId)).first();

        if (existing != null) {
            return new SpawnData().fromDocument(existing);
        }

        return null;
    }

    /**
     * Saves the provided SpawnData object to database
     * @param database MongoDB Database
     * @param data SpawnData
     */
    public static void saveSpawnData(MongoDB database, SpawnData data) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", data.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, data.toDocument());
        } else {
            collection.insertOne(data.toDocument());
        }
    }

    /**
     * Saves a collection of SpawnData to the database
     * @param database MongoDB Database
     * @param data Collection of SpawnData
     */
    public static void saveSpawnData(MongoDB database, Collection<SpawnData> data) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        data.forEach(spawnData -> {
            final Document existing = collection.find(Filters.eq("id", spawnData.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, spawnData.toDocument());
            } else {
                collection.insertOne(spawnData.toDocument());
            }
        });
    }
}