package com.llewkcor.ares.core.player.data.account;

import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.UUID;

public final class AccountDAO {
    private static final String NAME = "ares";
    private static final String COLL = "accounts";

    /**
     * Retrieves an Ares Account from the database
     * @param database Database
     * @param uniqueId Ares ID
     * @return Ares Account
     */
    public static AresAccount getAccountByAresID(MongoDB database, UUID uniqueId) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(Filters.eq("ares_id", uniqueId)).first();

        if (existing != null) {
            return new AresAccount().fromDocument(existing);
        } else {
            return null;
        }
    }

    /**
     * Retrieves an Ares Account from the database
     * @param database Database
     * @param bukkitId Bukkit UUID
     * @return Ares Account
     */
    public static AresAccount getAccountByBukkitID(MongoDB database, UUID bukkitId) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(Filters.eq("bukkit_id", bukkitId)).first();

        if (existing != null) {
            return new AresAccount().fromDocument(existing);
        } else {
            return null;
        }
    }

    /**
     * Retrieves an Ares Account from the database
     * @param database Database
     * @param username Bukkit Username
     * @return Ares Account
     */
    public static AresAccount getAccountByUsername(MongoDB database, String username) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(Filters.eq("username", username)).first();

        if (existing != null) {
            return new AresAccount().fromDocument(existing);
        } else {
            return null;
        }
    }

    /**
     * Saves an Ares Account to the database
     * @param database Database
     * @param account Ares Account
     */
    public static void saveAccount(MongoDB database, AresAccount account) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("ares_id", account.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, account.toDocument());
        } else {
            collection.insertOne(account.toDocument());
        }
    }
}