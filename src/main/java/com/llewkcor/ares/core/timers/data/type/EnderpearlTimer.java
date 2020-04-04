package com.llewkcor.ares.core.timers.data.type;

import com.llewkcor.ares.core.timers.data.PlayerTimer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class EnderpearlTimer extends PlayerTimer {
    public EnderpearlTimer() {
        super(null, PlayerTimerType.ENDERPEARL, 0);
    }

    public EnderpearlTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.ENDERPEARL, seconds);
    }

    public EnderpearlTimer(UUID owner, long milliseconds) {
        super(owner, PlayerTimerType.ENDERPEARL, milliseconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your enderpearls have been unlocked");
    }

    @Override
    public EnderpearlTimer fromDocument(Document document) {
        this.owner = (UUID)document.get("owner_id");
        this.type = PlayerTimerType.valueOf(document.getString("type"));
        this.expire = document.getLong("expire");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("owner_id", owner)
                .append("type", type.name())
                .append("expire", expire);
    }
}