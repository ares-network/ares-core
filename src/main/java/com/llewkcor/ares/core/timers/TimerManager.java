package com.llewkcor.ares.core.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.timer.Timer;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import com.llewkcor.ares.core.timers.event.HUDUpdateEvent;
import com.llewkcor.ares.core.timers.listener.TimerListener;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.stream.Collectors;

public final class TimerManager {
    @Getter public final Ares plugin;
    @Getter public final TimerHandler handler;
    @Getter public final BukkitTask timerTask;

    public TimerManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new TimerHandler(this);
        this.timerTask = new Scheduler(plugin).async(() -> {
            for (AresAccount account : plugin.getPlayerManager().getAccountRepository()) {
                final Set<PlayerTimer> expired = account.getTimers().stream().filter(Timer::isExpired).collect(Collectors.toSet());
                final Set<PlayerTimer> timers = account.getTimers().stream().filter(timer -> !timer.isExpired()).collect(Collectors.toSet());

                expired.forEach(timer -> new Scheduler(plugin).sync(() -> handler.finishTimer(timer)).run());

                new Scheduler(plugin).sync(() -> {
                    final Player player = Bukkit.getPlayer(account.getBukkitId());
                    final HUDUpdateEvent hudUpdateEvent = new HUDUpdateEvent(player);

                    if (player != null) {
                        if (!timers.isEmpty()) {
                            timers.stream().filter(activeTimer -> activeTimer.getType().isRender()).forEach(renderedTimer -> {
                                if (renderedTimer.getType().isDecimal()) {
                                    hudUpdateEvent.add(renderedTimer.getType().getDisplayName() + " " + ChatColor.RED + Time.convertToDecimal(renderedTimer.getExpire() - Time.now()) + "s");
                                } else {
                                    hudUpdateEvent.add(renderedTimer.getType().getDisplayName() + " " + ChatColor.RED + Time.convertToHHMMSS(renderedTimer.getExpire() - Time.now()));
                                }
                            });
                        }

                        Bukkit.getPluginManager().callEvent(hudUpdateEvent);

                        final String hud = Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(hudUpdateEvent.getEntries());
                        final PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + hud + "\"}"), (byte) 2);

                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }
                }).run();
            }
        }).repeat(0L, 1L).run();

        Bukkit.getPluginManager().registerEvents(new TimerListener(this), plugin);

        // Combat Logger
        plugin.registerCustomEntity("Villager", 120, EntityVillager.class, CombatLogger.class);
    }

    /**
     * Returns a Player Timer matching the provided Bukkit Player and Timer Type
     * @param player Player
     * @param type Timer Type
     * @return PlayerTimer
     */
    public PlayerTimer getTimer(Player player, PlayerTimerType type) {
        return getActiveTimers(player).stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    /**
     * Returns all active timers for the provided player
     * @param player Player
     * @return Set of PlayerTimer
     */
    public Set<PlayerTimer> getActiveTimers(Player player) {
        final AresAccount account = plugin.getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            return Sets.newConcurrentHashSet();
        }

        return account.getTimers().stream().filter(timer -> !timer.isExpired()).collect(Collectors.toSet());
    }

    /**
     * Returns true if the provided player has the provided timer
     * @param player Player
     * @param type Timer Type
     * @return True if timer is active
     */
    public boolean hasTimer(Player player, PlayerTimerType type) {
        return getActiveTimers(player).stream().anyMatch(timer -> timer.getType().equals(type));
    }
}