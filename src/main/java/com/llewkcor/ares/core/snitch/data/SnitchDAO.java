package com.llewkcor.ares.core.snitch.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class SnitchDAO {
    private static final String NAME = "ares";
    private static final String COLL = "snitches";

    /**
     * Returns an Immutable Collection of all snitches in the database
     * @param database Database
     * @return Immutable Collection of snitches
     */
    public static ImmutableCollection<Snitch> getSnitches(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Snitch> snitches = Lists.newArrayList();

        for (Document document : collection.find()) {
            snitches.add(new Snitch().fromDocument(document));
        }

        return ImmutableList.copyOf(snitches);
    }

    /**
     * Save a single Snitch instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param snitch Snitch
     */
    public static void saveSnitch(MongoDB database, Snitch snitch) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", snitch.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, snitch.toDocument());
        } else {
            collection.insertOne(snitch.toDocument());
        }
    }

    /**
     * Save a collection of Snitches to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param snitches Collection of Snitches
     */
    public static void saveSnitches(MongoDB database, Collection<Snitch> snitches) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        snitches.forEach(snitch -> {
            final Document existing = collection.find(Filters.eq("id", snitch.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, snitch.toDocument());
            } else {
                collection.insertOne(snitch.toDocument());
            }
        });
    }

    /**
     * Delete a snitch from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param snitch Snitch
     */
    public static void deleteSnitch(MongoDB database, Snitch snitch) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", snitch.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}