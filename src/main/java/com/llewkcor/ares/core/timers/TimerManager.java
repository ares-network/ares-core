package com.llewkcor.ares.core.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import com.llewkcor.ares.core.timers.listener.TimerListener;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TimerManager {
    @Getter public final Ares plugin;
    @Getter public final TimerHandler handler;
    @Getter public final Set<PlayerTimer> activePlayerTimers;
    @Getter public final BukkitTask timerTask;

    public TimerManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new TimerHandler(this);
        this.activePlayerTimers = Sets.newConcurrentHashSet();
        this.timerTask = new Scheduler(plugin).async(() -> {
            final List<PlayerTimer> expiredTimers = getExpiredTimers();

            // Finishes expired times
            expiredTimers.forEach(timer -> new Scheduler(plugin).sync(() -> handler.finishTimer(timer)).run());

            // Renders HUD
            new Scheduler(plugin).sync(() -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    final List<PlayerTimer> timers = getActiveTimers(player);
                    final List<String> entries = Lists.newArrayList();

                    if (timers.isEmpty()) {
                        continue;
                    }

                    timers.stream().filter(activeTimer -> activeTimer.getType().isRender()).forEach(renderedTimer -> {
                        if (renderedTimer.getType().isDecimal()) {
                            entries.add(renderedTimer.getType().getDisplayName() + " " + ChatColor.RED + Time.convertToDecimal(renderedTimer.getExpire() - Time.now()) + "s");
                        } else {
                            entries.add(renderedTimer.getType().getDisplayName() + " " + ChatColor.RED + Time.convertToHHMMSS(renderedTimer.getExpire() - Time.now()));
                        }
                    });

                    final String hud = Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(entries);
                    final PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + hud + "\"}"), (byte) 2);

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }).run();
        }).repeat(0L, 1L).run();

        Bukkit.getPluginManager().registerEvents(new TimerListener(this), plugin);
    }

    public PlayerTimer getTimer(Player player, PlayerTimerType type) {
        return getActiveTimers(player).stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    public ImmutableList<PlayerTimer> getActiveTimers(Player player) {
        return ImmutableList.copyOf(activePlayerTimers.stream().filter(timer -> timer.getOwner().equals(player.getUniqueId())).collect(Collectors.toList()));
    }

    public ImmutableList<PlayerTimer> getExpiredTimers() {
        return ImmutableList.copyOf(activePlayerTimers.stream().filter(timer -> timer.isExpired()).collect(Collectors.toList()));
    }

    public boolean hasTimer(Player player, PlayerTimerType type) {
        return getActiveTimers(player).stream().anyMatch(timer -> timer.getType().equals(type));
    }
}