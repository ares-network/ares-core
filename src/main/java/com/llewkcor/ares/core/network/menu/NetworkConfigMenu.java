package com.llewkcor.ares.core.network.menu;

import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public final class NetworkConfigMenu extends Menu {
    @Getter public final Network network;
    @Getter public Scheduler updateScheduler;
    @Getter public BukkitTask updateTask;

    public NetworkConfigMenu(Plugin plugin, Network network, Player player) {
        super(plugin, player, network.getName() + " Configuration", 1);
        this.network = network;
        this.updateScheduler = new Scheduler(plugin).sync(this::update).repeat(0L, 20L);
    }

    private void update() {
        clear();

        final boolean admin = player.hasPermission("arescore.admin");
        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You are no longer a part of " + network.getName());
            return;
        }

        if (networkMember != null && !networkMember.hasPermission(NetworkPermission.ADMIN) && !admin) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You no longer have the permissions to edit this network configuration");
            return;
        }

        final ItemStack passwordEnabledIcon = new ItemBuilder()
                .setMaterial(Material.STAINED_CLAY)
                .setData((network.getConfiguration().isPasswordEnabled()) ? (short)5 : (short)14)
                .setName(ChatColor.GOLD + "Password Access")
                .addLore(Arrays.asList(ChatColor.GRAY + "Allows members to join using a password only", ChatColor.RESET + " ", (network.getConfiguration().isPasswordEnabled()) ? ChatColor.GREEN + "This feature is enabled" : ChatColor.RED + "This feature is disabled"))
                .build();

        final ItemStack snitchEnabledIcon = new ItemBuilder()
                .setMaterial(Material.STAINED_CLAY)
                .setData((network.getConfiguration().isSnitchNotificationsEnabled()) ? (short)5 : (short)14)
                .setName(ChatColor.GOLD + "Snitch Notifications")
                .addLore(Arrays.asList(ChatColor.GRAY + "Allows your network to see all snitch notifications", ChatColor.RESET + " ", (network.getConfiguration().isSnitchNotificationsEnabled()) ? ChatColor.GREEN + "This feature is enabled" : ChatColor.RED + "This feature is disabled"))
                .build();

        addItem(new ClickableItem(passwordEnabledIcon, 3, click -> {
            if (network.getConfiguration().getPassword() == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "You need to add a password first. Type '" + ChatColor.GRAY + "/network password " + network.getName() + " <password>" + ChatColor.RED + "'");
                return;
            }

            if (network.getConfiguration().isPasswordEnabled()) {
                network.getConfiguration().setPasswordEnabled(false);
                network.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RED + " disabled " + ChatColor.YELLOW + "password access to " + network.getName());
                update();
                return;
            }

            network.getConfiguration().setPasswordEnabled(true);
            network.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " enabled " + ChatColor.YELLOW + "password access to " + network.getName());
            update();
        }));

        addItem(new ClickableItem(snitchEnabledIcon, 5, click -> {
            if (network.getConfiguration().isSnitchNotificationsEnabled()) {
                network.getConfiguration().setSnitchNotificationsEnabled(false);
                network.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RED + " disabled " + ChatColor.YELLOW + "snitch notifications for " + network.getName());
                update();
                return;
            }

            network.getConfiguration().setSnitchNotificationsEnabled(true);
            network.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " enabled " + ChatColor.YELLOW + "snitch notifications for " + network.getName());
            update();
        }));
    }

    @Override
    public void open() {
        super.open();
        this.updateTask = updateScheduler.run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
        updateTask.cancel();
        updateScheduler = null;
        updateTask = null;
    }
}