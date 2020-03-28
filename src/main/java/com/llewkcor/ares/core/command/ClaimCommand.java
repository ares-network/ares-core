package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.claim.data.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("claim")
public final class ClaimCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("reinforce|ctr")
    @Syntax("<network name>")
    @Description("Reinforce blocks you punch while holding a reinforcement material")
    @CommandCompletion("@networks")
    public void onReinforce(Player player, String network) {
        plugin.getClaimManager().getHandler().startReinforcements(player, network, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You are now in reinforcement mode");
                player.sendMessage(ChatColor.GREEN + "Punch blocks with your reinforcement material in-hand to claim the block");
                player.sendMessage(ChatColor.YELLOW + "When you're finished, type '/claim disable' to exit this mode");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("fortify|ctf")
    @Syntax("<network name>")
    @Description("Fortify blocks you place down")
    @CommandCompletion("@networks")
    public void onFortify(Player player, String network) {
        plugin.getClaimManager().getHandler().startFortifications(player, network, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You are now in fortification mode");
                player.sendMessage(ChatColor.GREEN + "As you place blocks your reinforcement material will be consumed");
                player.sendMessage(ChatColor.YELLOW + "When you're finished, type '/claim disable' to exit this mode");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("info|cti")
    @Description("Will give you information about blocks you click on")
    public void onInfo(Player player) {
        plugin.getClaimManager().getHandler().startInformation(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You are now in information mode");
                player.sendMessage(ChatColor.GREEN + "Right-click blocks to view their claim information");
                player.sendMessage(ChatColor.YELLOW + "When you're finished, type '/claim disable' to exit this mode");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("off|disable")
    @Description("Remove yourself from all claiming modes")
    public void onDisable(Player player) {
        plugin.getClaimManager().getHandler().disableSession(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "Disabled claim mode");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("material|materials|mat|mats")
    @Description("View a list of all possible claim materials")
    public void onMaterials(Player player) {
        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.BLUE + "Reinforcement Materials" + ChatColor.AQUA + ":");

        for (ClaimType type : ClaimType.values()) {
            player.sendMessage(ChatColor.AQUA + type.getDisplayName() + ChatColor.GRAY + ": " + type.getDurability() + " Durability, Matures in " + Time.convertToHHMMSS(type.getMatureTimeInSeconds() * 1000L));
        }

        player.sendMessage(ChatColor.RESET + " ");
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}