package com.llewkcor.ares.core;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.bridge.BridgeManager;
import com.llewkcor.ares.core.chat.ChatManager;
import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.command.*;
import com.llewkcor.ares.core.configs.ConfigManager;
import com.llewkcor.ares.core.listener.AresEventListener;
import com.llewkcor.ares.core.network.NetworkManager;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.prison.PrisonPearlManager;
import com.llewkcor.ares.core.snitch.SnitchManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Ares extends JavaPlugin {
    @Getter public ConfigManager configManager;
    @Getter public NetworkManager networkManager;
    @Getter public SnitchManager snitchManager;
    @Getter public ClaimManager claimManager;
    @Getter public PrisonPearlManager prisonPearlManager;

    @Getter protected MongoDB databaseInstance;
    @Getter protected BridgeManager bridgeManager;
    @Getter protected PaperCommandManager commandManager;
    @Getter protected ChatManager chatManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.networkManager = new NetworkManager(this);
        this.bridgeManager = new BridgeManager(this);
        this.snitchManager = new SnitchManager(this);
        this.claimManager = new ClaimManager(this);
        this.prisonPearlManager = new PrisonPearlManager(this);
        this.commandManager = new PaperCommandManager(this);
        this.chatManager = new ChatManager(this);

        configManager.load();

        this.databaseInstance = new MongoDB(configManager.getGeneralConfig().getDatabaseUri());
        databaseInstance.openConnection();

        // Load Data
        networkManager.getHandler().loadAll(true);
        snitchManager.getHandler().loadAll(true);
        claimManager.getHandler().loadAll(true);
        prisonPearlManager.getHandler().loadAll(true);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new AresEventListener(this), this);

        // Commands
        commandManager.enableUnstableAPI("help");

        commandManager.registerCommand(new NetworkCommand(this));
        commandManager.registerCommand(new AccountCommand(this));
        commandManager.registerCommand(new PrisonPearlCommand(this));
        commandManager.registerCommand(new SnitchCommand(this));
        commandManager.registerCommand(new ClaimCommand(this));

        commandManager.getCommandCompletions().registerCompletion("networks", c -> {
            final Player player = c.getPlayer();
            final List<String> networkNames = Lists.newArrayList();

            for (Network network : ((player == null) ? networkManager.getNetworkRepository() : networkManager.getNetworksByPlayer(player))) {
                networkNames.add(network.getName());
            }

            return ImmutableList.copyOf(networkNames);
        });

        // Cleanup Tasks
        snitchManager.getHandler().performEntryCleanup();
    }

    @Override
    public void onDisable() {
        claimManager.getHandler().saveAll(true);
        networkManager.getHandler().saveAll(true);
        snitchManager.getHandler().saveAll(true);
        prisonPearlManager.getHandler().saveAll(true);

        databaseInstance.closeConnection();
    }
}