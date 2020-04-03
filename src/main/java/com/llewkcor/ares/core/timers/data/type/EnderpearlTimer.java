package com.llewkcor.ares.core.timers.data.type;

import com.llewkcor.ares.core.timers.data.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class EnderpearlTimer extends PlayerTimer {
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
}