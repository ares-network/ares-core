package com.llewkcor.ares.core;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.acid.AcidManager;
import com.llewkcor.ares.core.alts.AltManager;
import com.llewkcor.ares.core.bastion.BastionManager;
import com.llewkcor.ares.core.chat.ChatManager;
import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.command.*;
import com.llewkcor.ares.core.configs.ConfigManager;
import com.llewkcor.ares.core.factory.FactoryManager;
import com.llewkcor.ares.core.listener.AresEventListener;
import com.llewkcor.ares.core.loggers.LoggerManager;
import com.llewkcor.ares.core.network.NetworkManager;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.player.PlayerManager;
import com.llewkcor.ares.core.prison.PrisonPearlManager;
import com.llewkcor.ares.core.snitch.SnitchManager;
import com.llewkcor.ares.core.spawn.SpawnManager;
import com.llewkcor.ares.core.timers.TimerManager;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class Ares extends JavaPlugin {
    @Getter public ConfigManager configManager;
    @Getter public NetworkManager networkManager;
    @Getter public SnitchManager snitchManager;
    @Getter public ClaimManager claimManager;
    @Getter public PrisonPearlManager prisonPearlManager;
    @Getter public SpawnManager spawnManager;
    @Getter public FactoryManager factoryManager;
    @Getter public AltManager altManager;
    @Getter public TimerManager timerManager;
    @Getter public LoggerManager loggerManager;
    @Getter public BastionManager bastionManager;
    @Getter public AcidManager acidManager;

    @Getter protected MongoDB databaseInstance;
    @Getter protected PlayerManager playerManager;
    @Getter protected PaperCommandManager commandManager;
    @Getter protected ChatManager chatManager;

    @Override
    public void onEnable() {
        // Managers
        this.configManager = new ConfigManager(this);

        // We load the config for any constructors that may take config values
        configManager.load();

        this.networkManager = new NetworkManager(this);
        this.playerManager = new PlayerManager(this);
        this.snitchManager = new SnitchManager(this);
        this.claimManager = new ClaimManager(this);
        this.prisonPearlManager = new PrisonPearlManager(this);
        this.commandManager = new PaperCommandManager(this);
        this.chatManager = new ChatManager(this);
        this.spawnManager = new SpawnManager(this);
        this.factoryManager = new FactoryManager(this);
        this.altManager = new AltManager(this);
        this.timerManager = new TimerManager(this);
        this.loggerManager = new LoggerManager(this);
        this.bastionManager = new BastionManager(this);
        this.acidManager = new AcidManager(this);

        this.databaseInstance = new MongoDB(configManager.getGeneralConfig().getDatabaseUri());
        databaseInstance.openConnection();

        // Load Data
        networkManager.getHandler().loadAll(true);
        snitchManager.getHandler().loadAll(true);
        claimManager.getHandler().loadAll(true);
        prisonPearlManager.getHandler().loadAll(true);
        factoryManager.getHandler().loadAll(true);
        bastionManager.getHandler().loadAll(true);
        acidManager.getHandler().loadAll(true);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new AresEventListener(this), this);

        // Commands
        commandManager.enableUnstableAPI("help");

        commandManager.registerCommand(new NetworkCommand(this));
        commandManager.registerCommand(new AccountCommand(this));
        commandManager.registerCommand(new PrisonPearlCommand(this));
        commandManager.registerCommand(new SnitchCommand(this));
        commandManager.registerCommand(new ClaimCommand(this));
        commandManager.registerCommand(new SpawnCommand(this));
        commandManager.registerCommand(new FactoryCommand(this));
        commandManager.registerCommand(new BastionCommand(this));
        commandManager.registerCommand(new AcidCommand(this));
        commandManager.registerCommand(new AresCommand(this));

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
        prisonPearlManager.getHandler().performPearlCleanup();
        networkManager.getHandler().performNetworkCleanup();
        acidManager.getHandler().performAcidCleanup();
    }

    @Override
    public void onDisable() {
        claimManager.getHandler().saveAll(true);
        networkManager.getHandler().saveAll(true);
        snitchManager.getHandler().saveAll(true);
        prisonPearlManager.getHandler().saveAll(true);
        factoryManager.getHandler().saveAll(true);
        bastionManager.getHandler().saveAll(true);
        acidManager.getHandler().saveAll(true);

        databaseInstance.closeConnection();
    }

    /**
     * Handles registering a custom entity
     * @param entityName Entity Name
     * @param entityId Entity ID
     * @param nms NMS Class
     * @param custom Custom Entity Class
     */
    public void registerCustomEntity(String entityName, int entityId, Class<? extends EntityInsentient> nms, Class<? extends EntityInsentient> custom) {
        try {
            final List<Map<?, ?>> dataMap = Lists.newArrayList();

            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMap.get(2).containsKey(entityId)){
                dataMap.get(0).remove(entityName);
                dataMap.get(2).remove(entityId);
            }

            final Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);

            method.setAccessible(true);
            method.invoke(null, custom, entityName, entityId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}