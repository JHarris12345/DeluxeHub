package net.zithium.deluxehub.action;

import net.zithium.deluxehub.DeluxeHubPlugin;
import org.bukkit.entity.Player;

public interface Action {

    String getIdentifier();

    void execute(DeluxeHubPlugin plugin, Player player, String data);
}
