package com.playares.core.alts.listener;

import com.playares.commons.util.general.IPS;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.alts.data.AltDAO;
import com.playares.core.alts.data.AltEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

@AllArgsConstructor
public final class LoginListener implements Listener {
    @Getter public Ares plugin;

    @EventHandler
    public void onLoginAttempt(AsyncPlayerPreLoginEvent event) {
        if (plugin.getDatabaseInstance() == null || !plugin.getDatabaseInstance().isConnected()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server is still loading");
            return;
        }

        final UUID uniqueId = event.getUniqueId();
        final long address = IPS.toLong(event.getAddress().getHostAddress());
        final AltEntry existingMatch = AltDAO.getAlt(plugin.getDatabaseInstance(), uniqueId, address);

        if (existingMatch == null) {
            final AltEntry entry = new AltEntry(uniqueId, address);
            AltDAO.saveAlt(plugin.getDatabaseInstance(), entry);
        } else {
            existingMatch.setLastSeen(Time.now());
            AltDAO.saveAlt(plugin.getDatabaseInstance(), existingMatch);
        }
    }
}