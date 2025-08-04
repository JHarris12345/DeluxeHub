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
            DeluxeHubPlugin.floodgate.sendForm(player.getUniqueId(), createServerSelector(player));

        } else {
            getPlugin().getActionManager().executeActions(player, actions);
        }
    }

    private SimpleForm createServerSelector(Player player) {
        return SimpleForm.builder().title(DeluxeHubPlugin.colour("&0&lRealm Selector"))
                .content("Select which realm you'd like to play")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Survival &8(%math_{redisbungee_greensurvival}+{redisbungee_bluesurvival}% online)")), FormImage.Type.PATH, "textures/items/diamond_pickaxe.png")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Skyblock &8(%redisbungee_skyblock% online)")), FormImage.Type.PATH, "textures/items/emerald.png")
                .button(PlaceholderAPI.setPlaceholders(null, DeluxeHubPlugin.colour("&0Factions &8(%redisbungee_factions% online)")), FormImage.Type.PATH, "textures/items/gold_sword.png")
                .validResultHandler(response -> {
                    switch (response.clickedButtonId()) {
                        case 0: // Survival
                            SimpleForm survivalSelector = SimpleForm.builder().title(DeluxeHubPlugin.colour("&0&lSurvival Realms"))
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
}
