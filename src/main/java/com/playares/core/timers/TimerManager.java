package com.playares.core.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.playares.commons.timer.Timer;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.loggers.entity.CombatLogger;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.timers.data.PlayerTimer;
import com.playares.core.timers.data.type.PlayerTimerType;
import com.playares.core.timers.event.HUDUpdateEvent;
import com.playares.core.timers.listener.TimerListener;
import com.playares.essentials.EssentialsService;
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
        this.timerTask = new Scheduler(plugin).sync(() -> {
            final EssentialsService essentialsService = (EssentialsService)plugin.getService(EssentialsService.class);

            for (AresPlayer profile : plugin.getPlayerManager().getPlayerRepository()) {
                final Set<PlayerTimer> expired = profile.getTimers().stream().filter(Timer::isExpired).collect(Collectors.toSet());
                final Set<PlayerTimer> timers = profile.getTimers().stream().filter(timer -> !timer.isExpired()).collect(Collectors.toSet());

                expired.forEach(timer -> new Scheduler(plugin).sync(() -> handler.finishTimer(timer)).run());

                final Player player = Bukkit.getPlayer(profile.getUniqueId());
                final HUDUpdateEvent hudUpdateEvent = new HUDUpdateEvent(player);

                if (player != null && !player.isDead()) {
                    if (essentialsService != null && essentialsService.getRebootManager().isRebootInProgress()) {
                        hudUpdateEvent.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Restart" + ChatColor.RESET + " " + ChatColor.RED + Time.convertToHHMMSS(essentialsService.getRebootManager().getTimeUntilReboot()));
                    }

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
        final AresPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return Sets.newConcurrentHashSet();
        }

        return profile.getTimers().stream().filter(timer -> !timer.isExpired()).collect(Collectors.toSet());
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