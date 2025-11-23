package net.zithium.deluxehub.action.actions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import org.bukkit.entity.Player;

/**
 * Replaced with {@link ProxyAction}
 */
@Deprecated(forRemoval = true)
public class BungeeAction implements Action {

    @Override
    public String getIdentifier() {
        return "BUNGEE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        plugin.getLogger().warning("The [BUNGEE] action is deprecated! Please use [PROXY] instead.");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player.getName());
        out.writeUTF(data);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
