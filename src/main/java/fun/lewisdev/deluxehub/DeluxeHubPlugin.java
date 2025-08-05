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
import fun.lewisdev.deluxehub.utility.TextUtil;
import fun.lewisdev.deluxehub.utility.UpdateChecker;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeluxeHubPlugin extends JavaPlugin {

    private static final int BSTATS_ID = 3151;

    public static DeluxeHubPlugin plugin;
    public static FloodgateApi floodgate;

    private ConfigManager configManager;
    private ActionManager actionManager;
    private HooksManager hooksManager;
    private CommandManager commandManager;
    private CooldownManager cooldownManager;
    private ModuleManager moduleManager;
    private InventoryManager inventoryManager;

    public void onEnable() {
        long start = System.currentTimeMillis();
        plugin = this;

        floodgate = FloodgateApi.getInstance();

        getLogger().log(Level.INFO, " _   _            _          _    _ ");
        getLogger().log(Level.INFO, "| \\ |_ |  | | \\/ |_ |_| | | |_)   _)");
        getLogger().log(Level.INFO, "|_/ |_ |_ |_| /\\ |_ | | |_| |_)   _)");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "Version: " + getDescription().getVersion());
        getLogger().log(Level.INFO, "Author: ItsLewizzz");
        getLogger().log(Level.INFO, "");

        // Check if using Spigot
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException ex) {
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getLogger().severe("DeluxeHub requires Spigot to run, you can download");
            getLogger().severe("Spigot here: https://www.spigotmc.org/wiki/spigot-installation/.");
            getLogger().severe("The plugin will now disable.");
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getPluginLoader().disablePlugin(this);
            return;
        }

        //MinecraftVersion.disableUpdateCheck();

        // Enable bStats metrics
        //new MetricsLite(this, BSTATS_ID);

        // Check plugin hooks
        hooksManager = new HooksManager(this);

        // Load config files
        configManager = new ConfigManager();
        configManager.loadFiles(this);

        // If there were any configuration errors we should not continue
        if (!getServer().getPluginManager().isPluginEnabled(this)) return;

        // Command manager
        commandManager = new CommandManager(this);
        commandManager.reload();

        // Cooldown manager
        cooldownManager = new CooldownManager();

        // Inventory (GUI) manager
        inventoryManager = new InventoryManager();
        if (!hooksManager.isHookEnabled("HEAD_DATABASE")) inventoryManager.onEnable(this);

        // Core plugin modules
        moduleManager = new ModuleManager();
        moduleManager.loadModules(this);

        // Action system
        actionManager = new ActionManager(this);

        // Register BungeeCord channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "Successfully loaded in " + (System.currentTimeMillis() - start) + "ms");
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
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
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

    public boolean isPlayerBedrock(UUID uuid) {
        return uuid.toString().startsWith("00000000-0000-0000");
    }

    public static DeluxeHubPlugin getInstance() {
        return plugin;
    }

    public static String colour(String string) {
        Pattern pattern = Pattern.compile("&?#[A-Fa-f0-9]{6}");
        Matcher matcher = pattern.matcher(string);
        String output = ChatColor.translateAlternateColorCodes('&', string);

        while (matcher.find()) {
            String color = string.substring(matcher.start(), matcher.end());
            output = output.replace(color, "" + net.md_5.bungee.api.ChatColor.of(color.replace("&", "")));
        }

        return output;
    }

    public SimpleForm createServerSelector(Player player) {
        return SimpleForm.builder().title(DeluxeHubPlugin.colour("&0&lRealm Selector"))
                .content("Select which realm you'd like to play")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Survival &8(%math_{redisbungee_greensurvival}+{redisbungee_bluesurvival}% online)")), FormImage.Type.PATH, "textures/items/diamond_pickaxe.png")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Skyblock &8(%redisbungee_skyblock% online)")), FormImage.Type.PATH, "textures/items/emerald.png")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Factions &8(%redisbungee_factions% online)")), FormImage.Type.PATH, "textures/items/gold_sword.png")
                .validResultHandler(response -> {
                    switch (response.clickedButtonId()) {
                        case 0: // Survival
                            SimpleForm survivalSelector = createSurvivalSelector(player);
                            DeluxeHubPlugin.floodgate.sendForm(player.getUniqueId(), survivalSelector);
                            break;

                        case 1: // Skyblock
                            DeluxeHubPlugin.getInstance().getActionManager().executeActions(player, Collections.singletonList("[BUNGEE] skyblock"));
                            break;

                        case 2: // Factions
                            DeluxeHubPlugin.getInstance().getActionManager().executeActions(player, Collections.singletonList("[BUNGEE] factions"));
                            break;
                    }
                })
                .build();
    }

    public SimpleForm createSurvivalSelector(Player player) {
        return SimpleForm.builder().title(DeluxeHubPlugin.colour("&0&lSurvival Realms"))
                .content("Choose your Survival realm")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Blue Survival &8(%redisbungee_bluesurvival% online)")), FormImage.Type.PATH, "textures/items/dye_powder_light_blue.png")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Green Survival &8(%redisbungee_greensurvival% online)")), FormImage.Type.PATH, "textures/items/dye_powder_lime.png")
                .button(DeluxeHubPlugin.colour("&cBack"))
                .validResultHandler(response2 -> {
                    switch (response2.clickedButtonId()) {
                        case 0: // Blue Survival
                            DeluxeHubPlugin.getInstance().getActionManager().executeActions(player, Collections.singletonList("[BUNGEE] bluesurvival"));
                            break;

                        case 1: // Green Survival
                            DeluxeHubPlugin.getInstance().getActionManager().executeActions(player, Collections.singletonList("[BUNGEE] greensurvival"));
                            break;

                        case 2: // Back
                            DeluxeHubPlugin.floodgate.sendForm(player.getUniqueId(), createServerSelector(player));
                            break;
                    }
                })
                .build();
    }
}