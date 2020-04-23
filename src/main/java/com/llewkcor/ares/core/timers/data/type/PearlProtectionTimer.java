package com.llewkcor.ares.core.timers.data.type;

import com.llewkcor.ares.core.timers.data.PlayerTimer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PearlProtectionTimer extends PlayerTimer {
    public PearlProtectionTimer() {
        super(null, PlayerTimerType.PEARL_PROTECTION, 0);
    }

    public PearlProtectionTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.PEARL_PROTECTION, seconds);
    }

    public PearlProtectionTimer(UUID owner, long milliseconds) {
        super(owner, PlayerTimerType.PEARL_PROTECTION, milliseconds);
    }

    @Override
    public PearlProtectionTimer fromDocument(Document document) {
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

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your Prison Pearl protection has expired");
    }
}
