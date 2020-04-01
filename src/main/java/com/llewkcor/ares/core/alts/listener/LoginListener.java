package com.llewkcor.ares.core.alts.listener;

import com.llewkcor.ares.commons.util.general.IPS;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.alts.data.AltDAO;
import com.llewkcor.ares.core.alts.data.AltEntry;
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