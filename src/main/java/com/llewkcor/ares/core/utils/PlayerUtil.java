package com.llewkcor.ares.core.utils;

import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.network.data.Network;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class PlayerUtil {
    /**
     * Returns true if the provided player is an enemy of any living entity in the provided collection
     * @param plugin Plugin
     * @param entities Living Entity
     * @param player Player
     * @return True if nearby enemy
     */
    public static boolean isNearbyEnemy(Ares plugin, Collection<Entity> entities, Player player) {
        final Collection<Network> networks = plugin.getNetworkManager().getNetworksByPlayer(player);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            final Player otherPlayer = (Player)entity;

            if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (otherPlayer.hasPermission("arescore.admin")) {
                continue;
            }

            if (networks.isEmpty()) {
                return true;
            }

            for (Network network : networks) {
                if (network.isMember(otherPlayer)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
