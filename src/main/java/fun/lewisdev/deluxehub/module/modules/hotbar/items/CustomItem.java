package fun.lewisdev.deluxehub.module.modules.hotbar.items;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.module.modules.hotbar.HotbarItem;
import fun.lewisdev.deluxehub.module.modules.hotbar.HotbarManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CustomItem extends HotbarItem {

    private List<String> actions;

    public CustomItem(HotbarManager hotbarManager, ItemStack item, int slot, String key) {
        super(hotbarManager, item, slot, key);
        actions = getPlugin().getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getStringList("custom_join_items.items." + key + ".actions");
    }

    @Override
    protected void onInteract(Player player) {
        // We want to show the custom server selector to bedrock players
        if (this.getKey().equals("server_selector") && DeluxeHubPlugin.getInstance().isPlayerBedrock(player.getUniqueId())) {
            DeluxeHubPlugin.floodgate.sendForm(player.getUniqueId(), DeluxeHubPlugin.plugin.createServerSelector(player));

        } else {
            getPlugin().getActionManager().executeActions(player, actions);
        }
    }
}
