package com.llewkcor.ares.core.prison.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class PrisonPearlDAO {
    private static final String NAME = "ares";
    private static final String COLL = "pearls";

    /**
     * Returns an Immutable Collection of all prison pearls in the database
     * @param database Database
     * @return Immutable Collection of prison pearls
     */
    public static ImmutableCollection<PrisonPearl> getPearls(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<PrisonPearl> prisonPearls = Lists.newArrayList();

        for (Document document : collection.find()) {
            prisonPearls.add(new PrisonPearl().fromDocument(document));
        }

        return ImmutableList.copyOf(prisonPearls);
    }

    /**
     * Save a single PrisonPearl instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param pearl Prison Pearl
     */
    public static void savePearl(MongoDB database, PrisonPearl pearl) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", pearl.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, pearl.toDocument());
        } else {
            collection.insertOne(pearl.toDocument());
        }
    }

    /**
     * Save a collection of Prison Pearls to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param pearls Collection of Prison Pearls
     */
    public static void savePearls(MongoDB database, Collection<PrisonPearl> pearls) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        pearls.forEach(pearl -> {
            final Document existing = collection.find(Filters.eq("id", pearl.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, pearl.toDocument());
            } else {
                collection.insertOne(pearl.toDocument());
            }
        });
    }

    /**
     * Delete a Prison Pearl from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param pearl Prison Pearl
     */
    public static void deletePearl(MongoDB database, PrisonPearl pearl) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", pearl.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}