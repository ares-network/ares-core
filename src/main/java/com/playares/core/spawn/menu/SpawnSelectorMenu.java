package com.playares.core.spawn.menu;

import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.Ares;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SpawnSelectorMenu extends Menu {
    private final Ares plugin;

    public SpawnSelectorMenu(Ares plugin, Player player) {
        super(plugin, player, "Spawn Selector", 1);
        this.plugin = plugin;
    }

    @Override
    public void open() {
        super.open();
        update();
    }

    private void update() {
        final ItemStack randomIcon = new ItemBuilder()
                .setMaterial(Material.GRASS)
                .setName(ChatColor.AQUA + "Enter the world at random")
                .addLore(ChatColor.GRAY + "Randomly places you in the world")
                .addLore(ChatColor.RESET + " ")
                .addLore(ChatColor.GRAY + "Grants:")
                .addLore(ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.GREEN + "Prison Pearl Protection")
                .addLore(ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.GOLD + "Starter Kit")
                .build();

        final ItemStack bedIcon = new ItemBuilder()
                .setMaterial(Material.BED)
                .setName(ChatColor.RED + "Spawn at your bed")
                .addLore(ChatColor.GRAY + "Spawn at your bed spawn location")
                .addLore(ChatColor.RESET + " ")
                .addLore(ChatColor.RED + "You will not receive any spawn perks")
                .build();

        final ItemStack requestIcon = new ItemBuilder()
                .setMaterial(Material.SKULL_ITEM)
                .setData((short)3)
                .setName(ChatColor.GOLD + "Request to be summoned" + ChatColor.GREEN + "" + ChatColor.BOLD + "(PREMIUM ONLY)")
                .addLore(ChatColor.GRAY + "Request to spawn at a friend's location")
                .addLore(ChatColor.RESET + " ")
                .addLore(ChatColor.RED + "You will not receive any spawn perks")
                .build();

        addItem(new ClickableItem(randomIcon, 2, click -> {
            plugin.getSpawnManager().getHandler().randomlySpawn(player, new SimplePromise() {
                @Override
                public void success() {
                    player.closeInventory();
                }

                @Override
                public void fail(String s) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + s);
                }
            });
        }));

        addItem(new ClickableItem(bedIcon, 4, click -> {
            plugin.getSpawnManager().getHandler().spawnBed(player, new SimplePromise() {
                @Override
                public void success() {
                    player.closeInventory();
                }

                @Override
                public void fail(String s) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + s);
                }
            });
        }));

        addItem(new ClickableItem(requestIcon, 6, click -> {
            player.closeInventory();

            if (!player.hasPermission("arescore.spawn.ott")) {
                player.sendMessage(ChatColor.RED + "This option is only available for " + ChatColor.GOLD + "premium" + ChatColor.RED + " users.");
                player.sendMessage(ChatColor.YELLOW + "You can purchase a rank at " + ChatColor.GREEN + "https://playares.com/store");
                return;
            }

            player.sendMessage(ChatColor.GREEN + "Send a teleport request by typing " + ChatColor.GOLD + "/spawn request <username>");
        }));

        fill(new ItemBuilder().setMaterial(Material.STAINED_GLASS_PANE).setData((short)15).build());
    }
}
