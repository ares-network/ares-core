package com.playares.core.player.menu;

import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.core.Ares;
import com.playares.core.player.data.AresPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public final class AresSettingsMenu extends Menu {
    @Getter public final AresPlayer.AresSettings settings;

    public AresSettingsMenu(Ares plugin, Player player, AresPlayer.AresSettings settings) {
        super(plugin, player, "Ares Settings", 1);
        this.settings = settings;
    }

    @Override
    public void open() {
        super.open();
        update();
    }

    private void update() {
        final ItemStack autoacceptIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_HELMET)
                .setName(ChatColor.GOLD + "Auto-accept network invitations")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to automatically", ChatColor.GRAY + "join any network that sends you an invitation", ChatColor.RESET + " ", (settings.isAutoAcceptNetworkInvites() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        final ItemStack snitchIcon = new ItemBuilder()
                .setMaterial(Material.JUKEBOX)
                .setName(ChatColor.GOLD + "Show Snitch Notifications")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to see", ChatColor.GRAY + "snitch notifications for any network you are in", ChatColor.RESET + " ", (settings.isSnitchNotificationsEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        addItem(new ClickableItem(autoacceptIcon, 0, click -> {
            settings.setAutoAcceptNetworkInvites(!settings.isAutoAcceptNetworkInvites());
            update();
        }));

        addItem(new ClickableItem(snitchIcon, 2, click -> {
            settings.setAutoAcceptNetworkInvites(!settings.isSnitchNotificationsEnabled());
            update();
        }));
    }
}