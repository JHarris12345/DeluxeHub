package fun.lewisdev.deluxehub.action.actions;

import com.tcoded.folialib.impl.PlatformScheduler;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.action.Action;
import org.bukkit.entity.Player;

public class CloseInventoryAction implements Action {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();

    @Override
    public String getIdentifier() {
        return "CLOSE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        scheduler.runAtEntity(player, task -> player.closeInventory());
    }
}
