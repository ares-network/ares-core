package com.playares.core;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.bridge.BridgeService;
import com.playares.commons.AresPlugin;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.services.account.AccountService;
import com.playares.commons.services.alts.AltWatcherService;
import com.playares.commons.services.customitems.CustomItemService;
import com.playares.commons.services.event.CustomEventService;
import com.playares.core.acid.AcidManager;
import com.playares.core.bastion.BastionManager;
import com.playares.core.chat.ChatManager;
import com.playares.core.claim.ClaimManager;
import com.playares.core.command.*;
import com.playares.core.compactor.CompactManager;
import com.playares.core.configs.ConfigManager;
import com.playares.core.factory.FactoryManager;
import com.playares.core.loggers.LoggerManager;
import com.playares.core.network.NetworkManager;
import com.playares.core.network.data.Network;
import com.playares.core.player.PlayerManager;
import com.playares.core.prison.PrisonPearlManager;
import com.playares.core.snitch.SnitchManager;
import com.playares.core.snitch.data.Snitch;
import com.playares.core.spawn.SpawnManager;
import com.playares.core.timers.TimerManager;
import com.playares.essentials.EssentialsService;
import com.playares.humbug.HumbugService;
import com.playares.luxe.LuxeService;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class Ares extends AresPlugin {
    @Getter public MongoDB databaseInstance;
    @Getter public PlayerManager playerManager;
    @Getter public ChatManager chatManager;
    @Getter public ConfigManager configManager;
    @Getter public NetworkManager networkManager;
    @Getter public SnitchManager snitchManager;
    @Getter public ClaimManager claimManager;
    @Getter public PrisonPearlManager prisonPearlManager;
    @Getter public SpawnManager spawnManager;
    @Getter public FactoryManager factoryManager;
    @Getter public TimerManager timerManager;
    @Getter public LoggerManager loggerManager;
    @Getter public BastionManager bastionManager;
    @Getter public AcidManager acidManager;
    @Getter public CompactManager compactManager;

    @Override
    public void onEnable() {
        // Managers
        this.configManager = new ConfigManager(this);

        // We load the config for any constructors that may take config values
        configManager.load();

        this.playerManager = new PlayerManager(this);
        this.networkManager = new NetworkManager(this);
        this.snitchManager = new SnitchManager(this);
        this.claimManager = new ClaimManager(this);
        this.prisonPearlManager = new PrisonPearlManager(this);
        this.chatManager = new ChatManager(this);
        this.spawnManager = new SpawnManager(this);
        this.factoryManager = new FactoryManager(this);
        this.timerManager = new TimerManager(this);
        this.loggerManager = new LoggerManager(this);
        this.bastionManager = new BastionManager(this);
        this.acidManager = new AcidManager(this);
        this.compactManager = new CompactManager(this);
        this.databaseInstance = new MongoDB(configManager.getGeneralConfig().getDatabaseUri());

        registerDatabase(databaseInstance);
        databaseInstance.openConnection();

        // Load Data
        networkManager.getHandler().loadAll(true);
        snitchManager.getHandler().loadAll(true);
        prisonPearlManager.getHandler().loadAll(true);
        factoryManager.getHandler().loadAll(true);
        factoryManager.getRecipeManager().loadRecipes();
        bastionManager.getHandler().loadAll(true);
        acidManager.getHandler().loadAll(true);

        // Commands
        registerCommandManager(new PaperCommandManager(this));
        commandManager.enableUnstableAPI("help");

        registerCommand(new NetworkCommand(this));
        registerCommand(new NetworkCommand(this));
        registerCommand(new PrisonPearlCommand(this));
        registerCommand(new SnitchCommand(this));
        registerCommand(new ClaimCommand(this));
        registerCommand(new SpawnCommand(this));
        registerCommand(new FactoryCommand(this));
        registerCommand(new BastionCommand(this));
        registerCommand(new AcidCommand(this));
        registerCommand(new AresCommand(this));
        registerCommand(new ChatCommand(this));
        registerCommand(new AHelpCommand(this));
        registerCommand(new CompactorCommand(this));
        registerCommand(new CombatCommand(this));
        registerCommand(new RegionCommand(this));

        // Protocol
        registerProtocolLibrary(ProtocolLibrary.getProtocolManager());

        // Services
        registerService(new CustomEventService(this));
        registerService(new AccountService(this, configManager.getGeneralConfig().getDatabaseName()));
        registerService(new CustomItemService(this));
        registerService(new EssentialsService(this, configManager.getGeneralConfig().getDatabaseName()));
        registerService(new HumbugService(this));
        registerService(new BridgeService(this));
        registerService(new AltWatcherService(this, configManager.getGeneralConfig().getDatabaseName()));
        registerService(new LuxeService(this, configManager.getGeneralConfig().getDatabaseName()));
        startServices();

        commandManager.getCommandCompletions().registerCompletion("networks", c -> {
            final Player player = c.getPlayer();
            final List<String> networkNames = Lists.newArrayList();

            for (Network network : ((player == null) ? networkManager.getNetworkRepository() : networkManager.getNetworksByPlayer(player))) {
                networkNames.add(network.getName());
            }

            return ImmutableList.copyOf(networkNames);
        });

        commandManager.getCommandCompletions().registerCompletion("snitches", c -> {
            final Player player = c.getPlayer();
            final List<String> snitchNames = Lists.newArrayList();

            if (player == null) {
                return ImmutableList.copyOf(snitchNames);
            }

            for (Network network : networkManager.getNetworksByPlayer(player)) {
                final List<Snitch> snitches = snitchManager.getSnitchByOwner(network);
                snitches.forEach(snitch -> snitchNames.add(snitch.getName()));
            }

            return ImmutableList.copyOf(snitchNames);
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
    }

    /**
     * Handles reloading all configuration files for this plugin
     */
    public void onReload() {
        configManager.reload();
        factoryManager.getRecipeManager().loadRecipes();
        spawnManager.getKitManager().getHandler().load();
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