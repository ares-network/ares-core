package com.playares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

@CommandAlias("bastion")
@AllArgsConstructor
public final class BastionCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("create")
    @Description("Create a new bastion")
    @Syntax("<network name>")
    @CommandCompletion("@networks")
    public void onCreate(Player player, String networkName) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.SPONGE)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Sponge");
            return;
        }

        plugin.getBastionManager().getHandler().createBastion(player, networkName, target, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Bastion has been created and is now maturing");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("near")
    @Description("Show nearby bastion fields")
    public void onNear(Player player) {
        plugin.getBastionManager().getHandler().showNear(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("info|i")
    @Description("View maturity information for the Bastion block you are looking at")
    public void onInfo(Player player) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.SPONGE)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Sponge");
            return;
        }

        plugin.getBastionManager().getHandler().showInfo(player, target, new SimplePromise() {
            @Override
            public void success() {}

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