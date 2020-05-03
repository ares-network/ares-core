package com.playares.core.stats.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class KillStat implements Stat, MongoDocument<KillStat> {
    @Getter public ObjectId objectId;
    @Getter public int map;
    @Getter public UUID slainUniqueId;
    @Getter public String slainUsername;
    @Getter public UUID killerUniqueId;
    @Getter public String killerUsername;
    @Getter public String description;
    @Getter public long createTime;

    public KillStat() {
        this.objectId = null;
        this.map = 0;
        this.slainUniqueId = null;
        this.slainUsername = null;
        this.killerUniqueId = null;
        this.killerUsername = null;
        this.description = null;
        this.createTime = Time.now();
    }

    public KillStat(int map, Player killer, Player slain, String description) {
        this.objectId = new ObjectId();
        this.map = map;
        this.slainUniqueId = slain.getUniqueId();
        this.slainUsername = slain.getName();
        this.killerUniqueId = killer.getUniqueId();
        this.killerUsername = killer.getName();
        this.description = description;
        this.createTime = Time.now();
    }

    @Override
    public KillStat fromDocument(Document document) {
        this.objectId = (ObjectId)document.get("_id");
        this.map = document.getInteger("map");
        this.slainUniqueId = UUID.fromString(document.getString("slain_uuid"));
        this.slainUsername = document.getString("slain_username");
        this.killerUniqueId = UUID.fromString(document.getString("killer_uuid"));
        this.killerUsername = document.getString("killer_username");
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
                .append("killer_uuid", killerUniqueId.toString())
                .append("killer_username", killerUsername)
                .append("description", description)
                .append("create", createTime);
    }
}