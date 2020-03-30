package com.llewkcor.ares.core.spawn.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class SpawnData implements MongoDocument<SpawnData> {
    @Getter public UUID uniqueId;
    @Getter @Setter public boolean spawned;
    @Getter @Setter public boolean sendToSpawnOnJoin;

    public SpawnData() {
        this.uniqueId = null;
        this.spawned = false;
        this.sendToSpawnOnJoin = true;
    }

    public SpawnData(Player player) {
        this.uniqueId = player.getUniqueId();
        this.spawned = false;
        this.sendToSpawnOnJoin = true;
    }

    @Override
    public SpawnData fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.spawned = document.getBoolean("spawned");
        this.sendToSpawnOnJoin = document.getBoolean("send_to_spawn");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("spawned", spawned)
                .append("send_to_spawn", sendToSpawnOnJoin);
    }
}