package com.llewkcor.ares.core.timers;

import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class TimerHandler {
    @Getter public final TimerManager manager;

    public void addTimer(Player player, PlayerTimer timer) {
        final PlayerTimer existing = manager.getTimer(player, timer.getType());

        if (existing != null) {
            existing.setExpire(timer.getExpire());
            existing.setFrozenTime(timer.getFrozenTime());
            return;
        }

        manager.getActivePlayerTimers().add(timer);
    }

    public void removeTimer(Player player, PlayerTimer timer) {
        manager.getActivePlayerTimers().remove(timer);
    }

    public void removeTimer(Player player, PlayerTimerType type) {
        final PlayerTimer existing = manager.getTimer(player, type);

        if (existing == null) {
            return;
        }

        manager.getActivePlayerTimers().remove(existing);
    }

    public void finishTimer(PlayerTimer timer) {
        timer.onFinish();
        manager.getActivePlayerTimers().remove(timer);
    }
}
