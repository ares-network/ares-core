package com.llewkcor.ares.core.factory.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class FactoryDAO {
    private static final String NAME = "ares";
    private static final String COLL = "factories";

    /**
     * Returns an Immutable Collection of all factories in the database
     * @param database Database
     * @return Immutable Collection of factories
     */
    public static ImmutableCollection<Factory> getFactories(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Factory> factories = Lists.newArrayList();

        for (Document document : collection.find()) {
            factories.add(new Factory().fromDocument(document));
        }

        return ImmutableList.copyOf(factories);
    }

    /**
     * Save a single Factory instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param factory Factory
     */
    public static void saveFactory(MongoDB database, Factory factory) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", factory.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, factory.toDocument());
        } else {
            collection.insertOne(factory.toDocument());
        }
    }

    /**
     * Save a collection of Factories to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param factories Collection of Factories
     */
    public static void saveFactories(MongoDB database, Collection<Factory> factories) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        factories.forEach(factory -> {
            final Document existing = collection.find(Filters.eq("id", factory.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, factory.toDocument());
            } else {
                collection.insertOne(factory.toDocument());
            }
        });
    }

    /**
     * Delete a factory from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param factory Factory
     */
    public static void deleteFactory(MongoDB database, Factory factory) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", factory.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}