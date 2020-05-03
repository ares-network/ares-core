package com.playares.core.stats.listener;

import com.playares.core.stats.StatsManager;
import com.playares.core.stats.data.DeathStat;
import com.playares.core.stats.data.KillStat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@AllArgsConstructor
public final class StatsListener implements Listener {
    @Getter public final StatsManager manager;

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();

        if (slain.getKiller() != null) {
            final Player killer = slain.getKiller();

            if (killer.getUniqueId().equals(slain.getUniqueId())) {
                return;
            }

            final KillStat stat = new KillStat(manager.getPlugin().getConfigManager().getGeneralConfig().getMapNumber(), killer, slain, ChatColor.stripColor(event.getDeathMessage().replace("RIP: ", "")));
            manager.setTrackedEvent(stat);
            return;
        }

        final DeathStat stat = new DeathStat(manager.getPlugin().getConfigManager().getGeneralConfig().getMapNumber(), slain, ChatColor.stripColor(event.getDeathMessage().replace("RIP: ", "")));
        manager.setTrackedEvent(stat);
    }
}