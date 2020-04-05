package com.llewkcor.ares.core.acid.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class AcidDAO {
    private static final String NAME = "ares";
    private static final String COLL = "acidBlocks";

    /**
     * Returns an Immutable Collection of all Acid Blocks in the database
     * @param database Database
     * @return Immutable Collection of Acid Blocks
     */
    public static ImmutableCollection<AcidBlock> getAcidBlocks(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final List<AcidBlock> acidBlocks = Lists.newArrayList();

        for (Document document : collection.find()) {
            acidBlocks.add(new AcidBlock().fromDocument(document));
        }

        return ImmutableList.copyOf(acidBlocks);
    }

    /**
     * Save a single Acid Block instance to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param acidBlock Acid Block
     */
    public static void saveAcidBlock(MongoDB database, AcidBlock acidBlock) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", acidBlock.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, acidBlock.toDocument());
        } else {
            collection.insertOne(acidBlock.toDocument());
        }
    }

    /**
     * Save a collection of Acid Blocks to the provided MongoDB instance
     * @param database MongoDB Instance
     * @param acidBlocks Collection of AcidBlocks
     */
    public static void saveAcidBlocks(MongoDB database, Collection<AcidBlock> acidBlocks) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);

        acidBlocks.forEach(acidBlock -> {
            final Document existing = collection.find(Filters.eq("id", acidBlock.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, acidBlock.toDocument());
            } else {
                collection.insertOne(acidBlock.toDocument());
            }
        });
    }

    /**
     * Delete a Acid Block from the provided MongoDB instance
     * @param database MongoDB Instance
     * @param acidBlock AcidBlock
     */
    public static void deleteAcidBlock(MongoDB database, AcidBlock acidBlock) {
        final MongoCollection<Document> collection = database.getCollection(NAME, COLL);
        final Document existing = collection.find(Filters.eq("id", acidBlock.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}
