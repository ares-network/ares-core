package com.playares.core.network.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.Ares;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;

public final class NetworkPlayerListMenu extends Menu {
    @Getter public final Ares ares;
    @Getter public final Network network;
    @Getter @Setter public int page;

    public NetworkPlayerListMenu(Ares plugin, Player player, Network network) {
        super(plugin, player, (network.getName().length() > 16 ? network.getName().substring(0, 15) : network.getName()) + " Members", 6);
        this.ares = plugin;
        this.network = network;
        this.page = 0;
    }

    @Override
    public void open() {
        super.open();
        update();
    }

    private void update() {
        clear();

        final List<NetworkMember> online = Lists.newArrayList(network.getOnlineMembers());
        final List<NetworkMember> members = Lists.newArrayList(network.getMembers());

        members.removeAll(online);

        online.sort(Comparator.comparing(NetworkMember::getUsername));
        members.sort(Comparator.comparing(NetworkMember::getUsername));

        final List<NetworkMember> merged = Lists.newArrayList();
        merged.addAll(online);
        merged.addAll(members);

        int cursor = 0;
        final int start = page * 52;
        final int end = start + 52;
        final boolean hasNextPage = merged.size() > end;
        final boolean hasPrevPage = start > 0;

        for (int i = start; i < end; i++) {
            if (cursor >= 52 || merged.size() <= i) {
                break;
            }

            final NetworkMember member = merged.get(i);

            if (member == null) {
                continue;
            }

            final List<String> permissions = Lists.newArrayList();

            for (NetworkPermission permission : NetworkPermission.values()) {
                final boolean hasPermission = member.hasPermission(permission);
                permissions.add(ChatColor.GOLD + permission.getDisplayName() + ChatColor.YELLOW + ": " + ((hasPermission || member.hasPermission(NetworkPermission.ADMIN)) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
            }

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setName(ChatColor.GOLD + member.getUsername() + (member.getBukkitPlayer() != null ? ChatColor.GREEN + " (Online)" : ChatColor.RED + " (Offline)"))
                    .setData((short)3)
                    .addLore(permissions)
                    .build();

            final SkullMeta meta = (SkullMeta)icon.getItemMeta();
            meta.setOwner(member.getUsername());
            icon.setItemMeta(meta);

            addItem(new ClickableItem(icon, cursor, click -> ares.getNetworkManager().getHandler().getMenuHandler().openPlayerEditMenu(player, network.getName(), member.getUsername(), new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void fail(String s) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "You do not have permission to perform this action");
                }
            })));

            cursor += 1;
        }

        if (hasNextPage) {
            final ItemStack nextPageIcon = new ItemBuilder().setMaterial(Material.EMERALD_BLOCK).setName(ChatColor.GREEN + "Next Page").build();
            addItem(new ClickableItem(nextPageIcon, 53, click -> setPage(page + 1)));
            update();
        }

        if (hasPrevPage) {
            final ItemStack prevPageIcon = new ItemBuilder().setMaterial(Material.REDSTONE_BLOCK).setName(ChatColor.RED + "Previous Page").build();
            addItem(new ClickableItem(prevPageIcon, 52, click -> setPage(page - 1)));
            update();
        }
    }
}