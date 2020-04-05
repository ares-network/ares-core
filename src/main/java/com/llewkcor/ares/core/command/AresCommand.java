package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("ares")
@AllArgsConstructor
public final class AresCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("version|build")
    @Description("Shows the current build of Ares this server is running")
    public void onVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "This server is running " + plugin.getDescription().getFullName());
    }

    @Subcommand("reload")
    @Description("Reload all Ares configuration files")
    @CommandPermission("arescore.admin")
    public void onReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        sender.sendMessage(ChatColor.GREEN + "All Ares configuration files have been reloaded");
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}