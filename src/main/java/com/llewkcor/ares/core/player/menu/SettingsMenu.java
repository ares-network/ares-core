package com.llewkcor.ares.core.player.menu;

import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.core.player.data.account.AresAccount;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public final class SettingsMenu extends Menu {
    @Getter public final AresAccount account;

    public SettingsMenu(Plugin plugin, Player player, AresAccount account) {
        super(plugin, player, "Your Ares Settings", 1);
        this.account = account;
    }

    public void update() {
        clear();

        final ItemStack broadcastIcon = new ItemBuilder()
                .setMaterial(Material.SIGN)
                .setName(ChatColor.GOLD + "Show Broadcasts")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to see", ChatColor.GRAY + "automatic server broadcasts & tips", ChatColor.RESET + " ", (account.getSettings().isBroadcastsEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        final ItemStack autoacceptIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_HELMET)
                .setName(ChatColor.GOLD + "Auto-accept network invitations")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to automatically", ChatColor.GRAY + "join any network that sends you an invitation", ChatColor.RESET + " ", (account.getSettings().isAutoAcceptNetworkInvites() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        final ItemStack snitchIcon = new ItemBuilder()
                .setMaterial(Material.JUKEBOX)
                .setName(ChatColor.GOLD + "Show Snitch Notifications")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to see", ChatColor.GRAY + "snitch notifications for any network you are in", ChatColor.RESET + " ", (account.getSettings().isSnitchNotificationsEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        final ItemStack pmIcon = new ItemBuilder()
                .setMaterial(Material.BOOK_AND_QUILL)
                .setName(ChatColor.GOLD + "Private Messages")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to receive", ChatColor.GRAY + "private messages from other players", ChatColor.RESET + " ", (account.getSettings().isPrivateMessagesEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        addItem(new ClickableItem(broadcastIcon, 0, click -> {
            if (account.getSettings().isBroadcastsEnabled()) {
                account.getSettings().setBroadcastsEnabled(false);
            } else {
                account.getSettings().setBroadcastsEnabled(true);
            }

            update();
        }));

        addItem(new ClickableItem(autoacceptIcon, 2, click -> {
            if (account.getSettings().isAutoAcceptNetworkInvites()) {
                account.getSettings().setAutoAcceptNetworkInvites(false);
            } else {
                account.getSettings().setAutoAcceptNetworkInvites(true);
            }

            update();
        }));

        addItem(new ClickableItem(snitchIcon, 4, click -> {
            if (account.getSettings().isSnitchNotificationsEnabled()) {
                account.getSettings().setSnitchNotificationsEnabled(false);
            } else {
                account.getSettings().setSnitchNotificationsEnabled(true);
            }

            update();
        }));

        addItem(new ClickableItem(pmIcon, 6, click -> {
            if (account.getSettings().isPrivateMessagesEnabled()) {
                account.getSettings().setPrivateMessagesEnabled(false);
            } else {
                account.getSettings().setPrivateMessagesEnabled(true);
            }

            update();
        }));
    }

    @Override
    public void open() {
        super.open();
        update();
    }
}