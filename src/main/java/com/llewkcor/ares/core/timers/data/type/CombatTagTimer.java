package com.llewkcor.ares.core.timers.data.type;

import com.llewkcor.ares.core.timers.data.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class CombatTagTimer extends PlayerTimer {
    public CombatTagTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.COMBAT, seconds);
    }

    public CombatTagTimer(UUID owner, long milliseconds) {
        super(owner, PlayerTimerType.COMBAT, milliseconds);
    }


    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "You are no longer combat-tagged");
    }
}