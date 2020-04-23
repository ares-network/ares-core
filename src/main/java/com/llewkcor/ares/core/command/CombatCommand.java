package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("combat|pvp")
public final class CombatCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("watch|timers|timer")
    @Description("View a GUI showing every player with active Combat-tag")
    @CommandPermission("arescore.admin")
    public void onWatch(Player player) {
        plugin.getTimerManager().getHandler().openCombatWatcher(player);
    }

    @Subcommand("enable")
    @Description("Remove any protections that prevent you from attacking others")
    public void onEnable(Player player) {
        plugin.getTimerManager().getHandler().removeProtections(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GOLD + "You have removed your " + ChatColor.GREEN + "Prison Pearl Protection");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }
}