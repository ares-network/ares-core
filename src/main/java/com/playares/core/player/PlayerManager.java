package com.playares.core.player;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.Ares;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.player.listener.PlayerListener;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter public final Ares plugin;
    @Getter public final PlayerHandler handler;
    @Getter public final Set<AresPlayer> playerRepository;

    public PlayerManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new PlayerHandler(this);
        this.playerRepository = Sets.newConcurrentHashSet();

        plugin.registerListener(new PlayerListener(this));
    }

    public AresPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public AresPlayer getPlayer(String username) {
        return playerRepository.stream().filter(player -> player.getUsername() != null && player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public AresPlayer getPlayerFromDatabase(Bson filter) {
        final MongoDB database = plugin.getDatabaseInstance();
        final MongoCollection<Document> collection = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "players");
        final Document existing = collection.find(filter).first();

        if (existing == null) {
            return null;
        }

        return new AresPlayer().fromDocument(existing);
    }

    /**
     * Handles saving all players in the Player Repository to the database
     * @param blocking Block the current thread
     */
    public void setAllPlayers(boolean blocking) {
        AresPlayer[] arr = new AresPlayer[playerRepository.size()];
        arr = playerRepository.toArray(arr);
        setPlayer(blocking, arr);
    }

    /**
     * Handles saving players to the database
     * @param blocking Block the current thread
     * @param players Players to save
     */
    public void setPlayer(boolean blocking, AresPlayer... players) {
        if (blocking) {
            for (AresPlayer player : players) {
                final MongoDB database = plugin.getDatabaseInstance();
                final MongoCollection<Document> collection = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "players");
                final Document existing = collection.find(Filters.eq("id", player.getUniqueId())).first();

                if (existing != null) {
                    collection.replaceOne(existing, player.toDocument());
                    continue;
                }

                collection.insertOne(player.toDocument());
            }

            Logger.print("Finished saving " + players.length + " Ares Players");
            return;
        }

        new Scheduler(plugin).async(() -> {
            for (AresPlayer player : players) {
                final MongoDB database = plugin.getDatabaseInstance();
                final MongoCollection<Document> collection = database.getCollection(plugin.getConfigManager().getGeneralConfig().getDatabaseName(), "players");
                final Document existing = collection.find(Filters.eq("id", player.getUniqueId())).first();

                if (existing != null) {
                    collection.replaceOne(existing, player.toDocument());
                    continue;
                }

                collection.insertOne(player.toDocument());
            }

            new Scheduler(plugin).sync(() -> Logger.print("Finished saving " + players.length + " Ares Players")).run();
        }).run();
    }
}
