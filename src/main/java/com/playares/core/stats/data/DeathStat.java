package com.playares.core.stats.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class DeathStat implements Stat, MongoDocument<DeathStat> {
    @Getter public ObjectId objectId;
    @Getter public int map;
    @Getter public UUID slainUniqueId;
    @Getter public String slainUsername;
    @Getter public String description;
    @Getter public long createTime;

    public DeathStat() {
        this.objectId = null;
        this.map = 0;
        this.slainUniqueId = null;
        this.slainUsername = null;
        this.description = null;
        this.createTime = Time.now();
    }

    public DeathStat(int map, Player slain, String description) {
        this.objectId = new ObjectId();
        this.map = map;
        this.slainUsername = slain.getName();
        this.slainUniqueId = slain.getUniqueId();
        this.description = description;
        this.createTime = Time.now();
    }

    @Override
    public DeathStat fromDocument(Document document) {
        this.objectId = (ObjectId)document.get("_id");
        this.map = document.getInteger("map");
        this.slainUniqueId = UUID.fromString("slain_uuid");
        this.slainUsername = document.getString("slain_username");
        this.description = document.getString("description");
        this.createTime = document.getLong("create");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("_id", objectId)
                .append("map", map)
                .append("slain_uuid", slainUniqueId.toString())
                .append("slain_username", slainUsername)
                .append("description", description)
                .append("create", createTime);
    }
}