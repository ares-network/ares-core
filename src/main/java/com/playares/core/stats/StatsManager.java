package com.playares.core.stats;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.promise.Promise;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.Ares;
import com.playares.core.stats.data.DeathStat;
import com.playares.core.stats.data.KillStat;
import com.playares.core.stats.data.Stat;
import com.playares.core.stats.listener.StatsListener;
import lombok.Getter;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public final class StatsManager {
    @Getter public final Ares plugin;

    public StatsManager(Ares plugin, int mapNumber) {
        this.plugin = plugin;

        plugin.registerListener(new StatsListener(this));
    }

    /**
     * Returns a promise containing every tracked kill event related to the provided UUID
     * @param uniqueId Bukkit UUID
     * @param promise Promise
     */
    public void getKills(UUID uniqueId, Promise<List<KillStat>> promise) {
        final List<KillStat> result = Lists.newArrayList();

        new Scheduler(plugin).async(() -> {

            final MongoDB database = (MongoDB)plugin.getDatabaseInstance(MongoDB.class);
            final MongoCollection<Document> collection = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "stats_kills");
            final MongoCursor<Document> cursor = collection.find(Filters.and(Filters.eq("map", plugin.getConfigManager().getGeneralConfig().getMapNumber()), Filters.eq("killer_uuid", uniqueId.toString()))).cursor();

            while (cursor.hasNext()) {
                final Document document = cursor.next();
                result.add(new KillStat().fromDocument(document));
            }

            new Scheduler(plugin).sync(() -> {

                promise.ready(result);

            }).run();

        }).run();
    }

    /**
     * Returns a promise containing every tracked event related to the provided UUID dying
     * @param uniqueId Bukkit UUID
     * @param promise Promise
     */
    public void getDeaths(UUID uniqueId, Promise<List<Stat>> promise) {
        final List<Stat> result = Lists.newArrayList();

        new Scheduler(plugin).async(() -> {

            final MongoDB database = (MongoDB)plugin.getDatabaseInstance(MongoDB.class);
            final MongoCollection<Document> kills = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "stats_kills");
            final MongoCollection<Document> deaths = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "stats_deaths");
            final MongoCursor<Document> killsCursor = kills.find(Filters.and(Filters.eq("map", plugin.getConfigManager().getGeneralConfig().getMapNumber()), Filters.eq("slain_uuid", uniqueId.toString()))).cursor();
            final MongoCursor<Document> deathsCursor = deaths.find(Filters.and(Filters.eq("map", plugin.getConfigManager().getGeneralConfig().getMapNumber()), Filters.eq("slain_uuid", uniqueId.toString()))).cursor();

            while (killsCursor.hasNext()) {
                final Document document = killsCursor.next();
                result.add(new KillStat().fromDocument(document));
            }

            while (deathsCursor.hasNext()) {
                final Document document = deathsCursor.next();
                result.add(new DeathStat().fromDocument(document));
            }

            new Scheduler(plugin).sync(() -> promise.ready(result)).run();

        }).run();
    }

    /**
     * Handles saving a tracked event to the database
     * @param event Tracked Event
     */
    public void setTrackedEvent(Stat event) {
        Document document = null;
        String collectionName = null;

        if (event instanceof KillStat) {
            final KillStat kill = (KillStat)event;
            document = kill.toDocument();
            collectionName = "stats_kills";
        }

        if (event instanceof DeathStat) {
            final DeathStat death = (DeathStat)event;
            document = death.toDocument();
            collectionName = "stats_deaths";
        }

        if (document == null) {
            return;
        }

        final Document finalDocument = document;
        final String finalCollectionName = collectionName;

        new Scheduler(plugin).async(() -> {

            final MongoDB database = (MongoDB)plugin.getDatabaseInstance(MongoDB.class);
            final MongoCollection<Document> collection = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), finalCollectionName);
            collection.insertOne(finalDocument);

        }).run();
    }
}
