package com.playares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.services.account.AccountService;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        plugin.onReload();
        sender.sendMessage(ChatColor.GREEN + "All Ares configuration files have been reloaded");
    }

    @Subcommand("settings")
    @Description("Access your Ares Account settings")
    public void onSettings(Player player) {
        final AccountService service = (AccountService)plugin.getService(AccountService.class);

        if (service == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain Account Service");
            return;
        }

        service.openSettingsMenu(player, new SimplePromise() {
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