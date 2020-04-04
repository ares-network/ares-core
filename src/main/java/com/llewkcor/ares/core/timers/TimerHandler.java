package com.llewkcor.ares.core.timers;

import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class TimerHandler {
    @Getter public final TimerManager manager;

    /**
     * Handles adding a new player timer
     * @param player Player
     * @param timer Timer
     */
    public void addTimer(Player player, PlayerTimer timer) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            return;
        }

        final PlayerTimer existing = manager.getTimer(player, timer.getType());

        if (existing != null) {
            existing.setExpire(timer.getExpire());
            existing.setFrozenTime(timer.getFrozenTime());
            return;
        }

        account.getTimers().add(timer);
    }

    /**
     * Handles removing a player timer of the provided type
     * @param player Player
     * @param timer Timer
     */
    public void removeTimer(Player player, PlayerTimer timer) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            return;
        }

        account.getTimers().remove(timer);
    }

    /**
     * Handles removing a player timer of the provided type
     * @param player Player
     * @param type Timer Type
     */
    public void removeTimer(Player player, PlayerTimerType type) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            return;
        }

        final PlayerTimer existing = manager.getTimer(player, type);

        account.getTimers().remove(existing);
    }

    /**
     * Handles finishing a Player Timer
     * @param timer Player Timer
     */
    public void finishTimer(PlayerTimer timer) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(timer.getOwner());

        if (account == null) {
            return;
        }

        timer.onFinish();
        account.getTimers().remove(timer);
    }
}
