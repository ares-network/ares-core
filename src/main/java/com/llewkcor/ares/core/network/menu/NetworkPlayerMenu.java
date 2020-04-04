package com.llewkcor.ares.core.network.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class NetworkPlayerMenu extends Menu {
    @Getter public final Network network;
    @Getter public final NetworkMember member;
    @Getter public Scheduler updateScheduler;
    @Getter public BukkitTask updateTask;

    public NetworkPlayerMenu(Ares plugin, Network network, NetworkMember member, Player player) {
        super(plugin, player, member.getUsername() + "'s Permissions", 2);
        this.network = network;
        this.member = member;
        this.updateScheduler = new Scheduler(plugin).sync(this::update).repeat(0L, 20L);
    }

    private void update() {
        clear();

        final boolean admin = player.hasPermission("arescore.admin");

        if (!network.isMember(member.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + member.getUsername() + " is not longer a member of " + member.getUsername());
            return;
        }

        if (!network.isMember(player) && !admin) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You are no longer a member of " + network.getName());
            return;
        }

        if (!member.hasPermission(NetworkPermission.ADMIN) && !admin) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You no longer have the proper permissions needed to perform this action");
            return;
        }

        int pos = 0;

        for (NetworkPermission permission : NetworkPermission.values()) {
            final boolean hasPermission = member.hasPermission(permission);
            final List<String> lore = Lists.newArrayList();

            lore.add(ChatColor.GRAY + permission.getDescription());
            lore.add(ChatColor.RESET + " ");
            lore.add(hasPermission ? ChatColor.GREEN + "Permission Enabled" : ChatColor.RED + "Permission Disabled");

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.STAINED_CLAY)
                    .setData(hasPermission ? (short)5 : (short)14)
                    .setName(ChatColor.GOLD + permission.getDisplayName())
                    .addLore(lore)
                    .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            addItem(new ClickableItem(icon, pos, click -> {
                if (hasPermission) {
                    member.revokePermission(permission);
                    update();
                    return;
                }

                member.grantPermission(permission);
                update();
            }));

            pos += 1;
        }
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
