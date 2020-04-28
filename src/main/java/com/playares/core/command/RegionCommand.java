package com.playares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@CommandAlias("region")
@AllArgsConstructor
public final class RegionCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("info|i")
    @CommandPermission("arescore.region")
    @Description("View information about a region")
    @Syntax("[region]")
    public void onInfo(Player player, @Optional String name) {

    }

    @Subcommand("create")
    @CommandPermission("arescore.region")
    @Description("Create a new region")
    @Syntax("<type> <name>")
    public void onCreate(Player player, @Values("staff") String type, @Single String name) {

    }

    @Subcommand("delete")
    @CommandPermission("arescore.region")
    @Description("Delete an existing region")
    @Syntax("<region>")
    public void onDelete(Player player, @Single String region) {

    }

    @Subcommand("addflag|af")
    @CommandPermission("arescore.region")
    @Description("Add a new flag to a region")
    @Syntax("<region> <flag>")
    public void onAddFlag(Player player, String region, @Single String flag) {

    }

    @Subcommand("remflag|rf")
    @CommandPermission("arescore.region")
    @Description("Remove a flag from a region")
    @Syntax("<region> <flag>")
    public void onRemFlag(Player player, String region, @Single String flag) {

    }

    @Subcommand("flags")
    @CommandPermission("arescore.region")
    @Description("View a list of all possible region flags")
    public void onListFlags(Player player) {

    }
}
