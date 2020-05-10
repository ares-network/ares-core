package com.playares.core.network.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.core.Ares;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class NetworkPlayerMenu extends Menu {
    @Getter public final Network network;
    @Getter public final NetworkMember member;

    public NetworkPlayerMenu(Ares plugin, Network network, NetworkMember member, Player player) {
        super(plugin, player, member.getUsername() + "'s Permissions", 2);
        this.network = network;
        this.member = member;
    }

    private void update() {
        clear();

        final boolean admin = player.hasPermission("arescore.admin");
        final NetworkMember editingMember = network.getMember(player);

        if (!network.isMember(member.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + member.getUsername() + " is not longer a member of " + network.getName());
            return;
        }

        if (editingMember == null && !admin) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You are no longer a member of " + network.getName());
            return;
        }

        if (editingMember != null && !editingMember.hasPermission(NetworkPermission.ADMIN) && !admin) {
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
                final Player playerMember = member.getBukkitPlayer();

                if (hasPermission) {
                    if (playerMember != null) {
                        playerMember.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " updated your permissions in " + ChatColor.BLUE + network.getName());
                        playerMember.sendMessage(ChatColor.GOLD + permission.getDisplayName() + ChatColor.YELLOW + ": " + ChatColor.RED + "Revoked");
                    }

                    member.revokePermission(permission);
                    update();
                    return;
                }

                if (playerMember != null) {
                    playerMember.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " updated your permissions in " + ChatColor.BLUE + network.getName());
                    playerMember.sendMessage(ChatColor.GOLD + permission.getDisplayName() + ChatColor.YELLOW + ": " + ChatColor.GREEN + "Granted");
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
        addUpdater(this::update, 10L);
    }
}
