package com.llewkcor.ares.core.claim.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class ClaimDAO {
    private static final String NAME = "ares";
    private static final String COLL = "claims";

    /**
     * Returns an Immutable Collection of all claims in the database
     * @param database Database
     * @return Immutable Collection of claims
     */
    public static ImmutableCollection<Claim> getClaims(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Claim> claims = Lists.newArrayList();

        for (Document document : collection.find()) {
            claims.add(new Claim().fromDocument(document));
        }

        return ImmutableList.copyOf(claims);
    }

    /**
     * Returns an Immutable Collection of all claims within the provided chunk coordinates
     * @param database Database
     * @param chunkX Chunk X
     * @param chunkZ Chunk Z
     * @return Immutable Collection of claims
     */
    public static ImmutableCollection<Claim> getChunkClaims(MongoDB database, int chunkX, int chunkZ) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<Claim> claims = Lists.newArrayList();

        for (Document document : collection.find(Filters.and(Filters.eq("chunk_x", chunkX), Filters.eq("chunk_z", chunkZ)))) {
            claims.add(new Claim().fromDocument(document));
        }

        return ImmutableList.copyOf(claims);
    }

    /**
     * Save a single Claim instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param claim Claim
     */
    public static void saveClaim(MongoDB database, Claim claim) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, claim.toDocument());
        } else {
            collection.insertOne(claim.toDocument());
        }
    }

    /**
     * Save a collection of Claims to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param claims Collection of Claims
     */
    public static void saveClaims(MongoDB database, Collection<Claim> claims) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        claims.forEach(claim -> {
            final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, claim.toDocument());
            } else {
                collection.insertOne(claim.toDocument());
            }
        });
    }

    /**
     * Delete a claim from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param claim Claim
     */
    public static void deleteClaim(MongoDB database, Claim claim) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}