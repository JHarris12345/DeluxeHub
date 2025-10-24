package fun.lewisdev.deluxehub.inventory;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.inventory.inventories.CustomGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryManager {

    private DeluxeHubPlugin plugin;
    private final Map<String, AbstractInventory> inventories;

    public InventoryManager() {
        inventories = new ConcurrentHashMap<>();
    }

    public void onEnable(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
        loadCustomMenus();
        inventories.values().forEach(AbstractInventory::onEnable);
        plugin.getServer().getPluginManager().registerEvents(new InventoryListener(), plugin);
    }

    private void loadCustomMenus() {
        File directory = new File(plugin.getDataFolder(), "menus");
        createMenusDirectoryIfNeeded(directory);

        // Load all menu files
        File[] yamlFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (yamlFiles == null) return;

        for (File file : yamlFiles) {
            String name = file.getName().replace(".yml", "");
            if (inventories.containsKey(name)) {
                plugin.getLogger().warning("Inventory with name '" + file.getName() + "' already exists, skipping duplicate..");
                continue;
            }

            try {
                CustomGUI customGUI = new CustomGUI(plugin, YamlConfiguration.loadConfiguration(file));
                inventories.put(name, customGUI);
                plugin.getLogger().info("Loaded custom menu '" + name + "'.");
            } catch (Exception e) {
                plugin.getLogger().severe("Could not load file '" + name + "' (YAML error).");
                e.printStackTrace();
            }
        }
    }

    private void createMenusDirectoryIfNeeded(File directory) {
        if (!directory.exists()) {
            directory.mkdir();
            File file = new File(directory, "serverselector.yml");
            if (!file.exists()) {
                try (InputStream inputStream = this.plugin.getResource("serverselector.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        plugin.getLogger().warning("Resource 'serverselector.yml' not found.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addInventory(String key, AbstractInventory inventory) {
        inventories.put(key, inventory);
    }

    public Map<String, AbstractInventory> getInventories() {
        return inventories;
    }

    public Optional<AbstractInventory> getInventory(String key) {
        return Optional.ofNullable(inventories.get(key));
    }

    public void onDisable() {
        inventories.values().forEach(abstractInventory -> {
            abstractInventory.getOpenInventories().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.closeInventory();
                }
            });
            abstractInventory.getOpenInventories().clear();
        });
        inventories.clear();
    }
}
