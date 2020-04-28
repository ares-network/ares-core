package com.playares.core.bastion.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.connect.mongodb.MongoDB;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class BastionDAO {
    private static final String NAME = "ares";
    private static final String COLL = "bastions";

    /**
     * Returns an Immutable Collection of all bastions in the database
     * @param database Database
     * @return Immutable Collection of bastions
     */
    public static ImmutableCollection<Bastion> getBastions(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Bastion> bastions = Lists.newArrayList();

        for (Document document : collection.find()) {
            bastions.add(new Bastion().fromDocument(document));
        }

        return ImmutableList.copyOf(bastions);
    }

    /**
     * Save a single Bastion instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param bastion Bastion
     */
    public static void saveBastion(MongoDB database, Bastion bastion) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", bastion.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, bastion.toDocument());
        } else {
            collection.insertOne(bastion.toDocument());
        }
    }

    /**
     * Save a collection of Bastions to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param bastions Collection of Bastions
     */
    public static void saveBastions(MongoDB database, Collection<Bastion> bastions) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        bastions.forEach(bastion -> {
            final Document existing = collection.find(Filters.eq("id", bastion.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, bastion.toDocument());
            } else {
                collection.insertOne(bastion.toDocument());
            }
        });
    }

    /**
     * Delete a bastion from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param bastion Bastion
     */
    public static void deleteBastion(MongoDB database, Bastion bastion) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", bastion.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}
