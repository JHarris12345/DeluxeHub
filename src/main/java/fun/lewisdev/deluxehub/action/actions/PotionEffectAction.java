package fun.lewisdev.deluxehub.action.actions;

import com.cryptomorin.xseries.XPotion;
import com.tcoded.folialib.impl.PlatformScheduler;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.action.Action;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PotionEffectAction implements Action {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();

    @Override
    public String getIdentifier() {
        return "EFFECT";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String[] args = data.split(";");
        scheduler.runAtEntity(player, task -> {
            PotionEffect effect = XPotion.matchXPotion(args[0]).get()
                    .buildPotionEffect(
                        PotionEffect.INFINITE_DURATION, 
                        Integer.parseInt(args[1]) - 1
                    );
            player.addPotionEffect(effect);
        });
    }
}
