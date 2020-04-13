package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class CompactorCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @CommandAlias("compact|compactor")
    @Description("Compact the item in your hand")
    public void onCompact(Player player) {
        plugin.getCompactManager().getHandler().compact(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Item has been compacted");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @CommandAlias("decompact|decompactor")
    @Description("Decompact the item in your hand")
    public void onDecompact(Player player) {
        plugin.getCompactManager().getHandler().decompact(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Item has been decompacted");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }
}