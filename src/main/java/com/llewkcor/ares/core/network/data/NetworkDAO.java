package com.llewkcor.ares.core.network.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class NetworkDAO {
    private static final String NAME = "ares";
    private static final String COLL = "networks";

    /**
     * Returns an Immutable Collection of all networks in the database
     * @param database Database
     * @return Immutable Collection of networks
     */
    public static ImmutableCollection<Network> getNetworks(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Network> networks = Lists.newArrayList();

        for (Document document : collection.find()) {
            networks.add(new Network().fromDocument(document));
        }

        return ImmutableList.copyOf(networks);
    }

    /**
     * Saves the provided Network to the provided MongoDB instance
     * @param database MongoDB instance
     * @param network Network
     */
    public static void saveNetwork(MongoDB database, Network network) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", network.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, network.toDocument());
        } else {
            collection.insertOne(network.toDocument());
        }
    }

    /**
     * Saves a collection of networks to the provided MongoDB instance
     * @param database MongoDB Database
     * @param networks Networks Collection
     */
    public static void saveNetworks(MongoDB database, Collection<Network> networks) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        networks.forEach(network -> {
            final Document existing = collection.find(Filters.eq("id", network.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, network.toDocument());
            } else {
                collection.insertOne(network.toDocument());
            }
        });
    }
}
