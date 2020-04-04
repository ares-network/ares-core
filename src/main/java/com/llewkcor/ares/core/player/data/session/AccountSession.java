package com.llewkcor.ares.core.player.data.session;

import com.llewkcor.ares.commons.util.general.Time;

import java.util.UUID;

public interface AccountSession {
    /**
     * Returns the Ares Account UUID
     * @return Ares Account UUID
     */
    UUID getUniqueId();

    /**
     * Returns the Bukkit UUID
     * @return Bukkit UUID
     */
    UUID getBukkitId();

    /**
     * Returns the Bukkit Username
     * @return
     */
    String getUsername();

    /**
     * Returns the time of expire in milliseconds
     * @return Expire time
     */
    long getExpireTime();

    /**
     * Returns true if this session is now expired
     * @return True if expired
     */
    default boolean isExpired() {
        return getExpireTime() <= Time.now();
    }
}
