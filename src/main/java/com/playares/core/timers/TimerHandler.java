package com.playares.core.timers;

import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.timers.data.PlayerTimer;
import com.playares.core.timers.data.type.PearlProtectionTimer;
import com.playares.core.timers.data.type.PlayerTimerType;
import com.playares.core.timers.menu.CombatMenu;
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
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final PlayerTimer existing = manager.getTimer(player, timer.getType());

        if (existing != null) {
            existing.setExpire(timer.getExpire());
            existing.setFrozenTime(timer.getFrozenTime());
            return;
        }

        profile.getTimers().add(timer);
    }

    /**
     * Handles removing a player timer of the provided type
     * @param player Player
     * @param timer Timer
     */
    public void removeTimer(Player player, PlayerTimer timer) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        profile.getTimers().remove(timer);
    }

    /**
     * Handles removing a player timer of the provided type
     * @param player Player
     * @param type Timer Type
     */
    public void removeTimer(Player player, PlayerTimerType type) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final PlayerTimer existing = manager.getTimer(player, type);

        profile.getTimers().remove(existing);
    }

    /**
     * Handles finishing a Player Timer
     * @param timer Player Timer
     */
    public void finishTimer(PlayerTimer timer) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(timer.getOwner());

        if (profile == null) {
            return;
        }

        if (!profile.getTimers().contains(timer)) {
            return;
        }

        timer.onFinish();
        profile.getTimers().remove(timer);
    }

    /**
     * Handles opening a Combat Watcher Menu
     * @param player Player
     */
    public void openCombatWatcher(Player player) {
        final CombatMenu menu = new CombatMenu(manager.getPlugin(), player);
        menu.open();
    }

    /**
     * Handles removing all PvP protections from a player
     * @param player Player
     * @param promise Promise
     */
    public void removeProtections(Player player, SimplePromise promise) {
        final PearlProtectionTimer timer = (PearlProtectionTimer)manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            promise.fail("You do not have any protections applied to your account");
            return;
        }

        removeTimer(player, timer);
        Logger.print(player.getName() + " removed their Prison Pearl Protection");
        promise.success();
    }
}