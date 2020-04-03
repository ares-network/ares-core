package com.llewkcor.ares.core.timers.data.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum PlayerTimerType {
    ENDERPEARL(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Enderpearl", true, true),
    COMBAT(ChatColor.RED + "" + ChatColor.BOLD + "Combat", true, true),
    CRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Crapple", true, true),
    GAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Gapple", false, false);

    @Getter public final String displayName;
    @Getter public final boolean render;
    @Getter public final boolean decimal;

    /**
     * Returns a PlayerTimerType enum matching the provided name
     * @param name Name
     * @return PlayerTimerType
     */
    public static PlayerTimerType match(String name) {
        final PlayerTimerType type;

        try {
            type = PlayerTimerType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        return type;
    }
}
