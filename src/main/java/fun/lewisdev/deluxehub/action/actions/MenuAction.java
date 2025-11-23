package fun.lewisdev.deluxehub.action.actions;

import com.tcoded.folialib.impl.PlatformScheduler;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.action.Action;
import org.bukkit.entity.Player;

public class MenuAction implements Action {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();

    @Override
    public String getIdentifier() {
        return "MENU";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        plugin.getInventoryManager().getInventory(data).ifPresentOrElse(
                inventory -> scheduler.runAtEntity(player, task -> inventory.openInventory(player)),
                () -> plugin.getLogger().warning("[MENU] Action Failed: Menu '" + data + "' not found.")
        );
    }
}
