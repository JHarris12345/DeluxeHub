package fun.lewisdev.deluxehub;

import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import cl.bgmp.minecraft.util.commands.exceptions.CommandPermissionsException;
import cl.bgmp.minecraft.util.commands.exceptions.CommandUsageException;
import cl.bgmp.minecraft.util.commands.exceptions.MissingNestedCommandException;
import cl.bgmp.minecraft.util.commands.exceptions.WrappedCommandException;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import fun.lewisdev.deluxehub.action.ActionManager;
import fun.lewisdev.deluxehub.command.CommandManager;
import fun.lewisdev.deluxehub.config.ConfigManager;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.cooldown.CooldownManager;
import fun.lewisdev.deluxehub.hook.HooksManager;
import fun.lewisdev.deluxehub.inventory.InventoryManager;
import fun.lewisdev.deluxehub.module.ModuleManager;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.hologram.HologramManager;
import fun.lewisdev.deluxehub.utility.UpdateChecker;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public class DeluxeHubPlugin extends JavaPlugin {

    private static final int BSTATS_ID = 26336;

    private ConfigManager configManager;
    private ActionManager actionManager;
    private HooksManager hooksManager;
    private CommandManager commandManager;
    private CooldownManager cooldownManager;
    private ModuleManager moduleManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        getLogger().info(" _   _            _          _    _ ");
        getLogger().info("| \\ |_ |  | | \\/ |_ |_| | | |_)   _)");
        getLogger().info("|_/ |_ |_ |_| /\\ |_ | | |_| |_)   _)");
        getLogger().info("");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: ItsLewizzz");
        getLogger().info("");

        // Ensure we're running on Spigot
        if (!isSpigotEnvironment()) {
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getLogger().severe("DeluxeHub requires Spigot to run.");
            getLogger().severe("Download it here: https://www.spigotmc.org/wiki/spigot-installation/");
            getLogger().severe("Plugin will now disable.");
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MinecraftVersion.disableUpdateCheck();

        // Metrics
        new MetricsLite(this, BSTATS_ID);

        // Hooks and config
        hooksManager = new HooksManager(this);

        configManager = new ConfigManager();
        configManager.loadFiles(this);

        if (!getServer().getPluginManager().isPluginEnabled(this)) return;

        // Core managers
        commandManager = new CommandManager(this);
        commandManager.reload();

        cooldownManager = new CooldownManager();

        inventoryManager = new InventoryManager();
        inventoryManager.onEnable(this);

        // Fallback inventory loading if HEAD_DATABASE is not enabled
        if (!hooksManager.isHookEnabled("HEAD_DATABASE")) {
            inventoryManager.onEnable(this);
        }

        moduleManager = new ModuleManager();
        moduleManager.loadModules(this);

        actionManager = new ActionManager(this);

        // Optional update check
        if (getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getBoolean("update-check")) {
            new UpdateChecker(this).checkForUpdate();
        }

        // BungeeCord channel registration
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("");
        getLogger().info("Successfully loaded in " + (System.currentTimeMillis() - start) + "ms");
    }

    private boolean isSpigotEnvironment() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        moduleManager.unloadModules();
        inventoryManager.onDisable();
        configManager.saveFiles();

    }

    public void reload() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        configManager.reloadFiles();

        inventoryManager.onDisable();
        inventoryManager.onEnable(this);

        getCommandManager().reload();

        moduleManager.loadModules(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        try {
            getCommandManager().execute(cmd.getName(), args, sender);
        } catch (CommandPermissionsException e) {
            Messages.NO_PERMISSION.send(sender);
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            //sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + "Usage: " + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An internal error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    public HologramManager getHologramManager() {
        return (HologramManager) moduleManager.getModule(ModuleType.HOLOGRAMS);
    }

    public HooksManager getHookManager() {
        return hooksManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }
}