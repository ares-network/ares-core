package com.llewkcor.ares.core.timers.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.timer.Timer;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.timers.data.type.CombatTagTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class CombatMenu extends Menu {
    @Getter public final Ares ares;
    @Getter public BukkitTask updater;

    public CombatMenu(Ares plugin, Player player) {
        super(plugin, player, "Combat-tagged Players", 6);
        this.ares = plugin;
    }

    @Override
    public void open() {
        super.open();
        this.updater = new Scheduler(plugin).sync(this::update).repeat(0L, 20L).run();
    }

    private void update() {
        clear();

        int cursor = 0;
        final List<CombatTagTimer> timers = Lists.newArrayList();

        for (Player online : Bukkit.getOnlinePlayers()) {
            final CombatTagTimer timer = (CombatTagTimer)ares.getTimerManager().getTimer(online, PlayerTimerType.COMBAT);

            if (timer == null) {
                continue;
            }

            timers.add(timer);
        }

        timers.sort(Comparator.comparingLong(Timer::getRemaining));
        Collections.reverse(timers);

        for (CombatTagTimer timer : timers) {
            final Player owner = Bukkit.getPlayer(timer.getOwner());

            if (owner == null) {
                continue;
            }

            final ItemStack head = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setData((short)3)
                    .setName(ChatColor.GOLD + owner.getName())
                    .addLore(ChatColor.RED + "Combat-tag: " + ChatColor.BLUE + Time.convertToHHMMSS(timer.getRemaining()))
                    .build();

            final ItemMeta meta = head.getItemMeta();
            final SkullMeta skullMeta = (SkullMeta)meta;

            skullMeta.setOwner(owner.getName());
            head.setItemMeta(skullMeta);

            addItem(new ClickableItem(head, cursor, click -> {
                player.teleport(owner);
                player.sendMessage(ChatColor.YELLOW + "Teleported to " + ChatColor.RED + owner.getName());
            }));

            cursor += 1;
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (this.updater != null) {
            this.updater.cancel();
            this.updater = null;
        }
    }
}
