package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("spawn")
@AllArgsConstructor
public final class SpawnCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @CommandAlias("spawn")
    @Description("Randomly spawn on the map")
    public void onSpawn(Player player) {
        plugin.getSpawnManager().getHandler().randomlySpawn(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + "NI " + ChatColor.GRAY + "You awake in a mysterious place..." + ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + " GR");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("set")
    @Description("Set the location of Spawn")
    @CommandPermission("arescore.spawn.set")
    public void onSet(Player player) {
        plugin.getSpawnManager().getHandler().updateSpawn(player);
    }

    @Subcommand("request")
    @Syntax("<username>")
    @Description("Request to teleport to a player out of spawn")
    public void onRequest(Player player, String username) {
        plugin.getSpawnManager().getHandler().sendTeleportRequest(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Your request has been sent");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("bed")
    @Description("Spawn at the last location you slept")
    public void onSpawnBed(Player player) {
        plugin.getSpawnManager().getHandler().spawnBed(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + "NI " + ChatColor.GRAY + "You awake in your bed..." + ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + " GR");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("accept")
    @Syntax("<username>")
    @Description("Accept a teleport request that has been sent to you")
    public void onAccept(Player player, String username) {
        plugin.getSpawnManager().getHandler().acceptTeleportRequest(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Summoning player to your location...");
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
