package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

@AllArgsConstructor
@CommandAlias("snitch|jukealert|ja")
public final class SnitchCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("create")
    @Syntax("<network name> <snitch name>")
    @Description("Create a new snitch for your network")
    @CommandCompletion("@networks")
    public void onCreate(Player player, String network, String name) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.JUKEBOX)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Jukebox");
            return;
        }

        plugin.getSnitchManager().getHandler().createSnitch(player, target, network, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Snitch has been created and is now maturing");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("log")
    @Description("View logs for the snitch you are looking at")
    public void onLog(Player player) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.JUKEBOX)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Jukebox");
            return;
        }

        plugin.getSnitchManager().getHandler().viewLogs(player, target, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("clear")
    @Description("Clear the logs for the snitch you are looking at")
    public void onClear(Player player) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.JUKEBOX)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Jukebox");
            return;
        }

        plugin.getSnitchManager().getHandler().clearLogs(player, target, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Snitch log entries have been cleared");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
