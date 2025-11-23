package fun.lewisdev.deluxehub.module.modules.visual.scoreboard;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.utility.PlaceholderUtil;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * Packet-based scoreboard handler
 */
public class ScoreHelper {

    private static final DeluxeHubPlugin plugin = JavaPlugin.getPlugin(DeluxeHubPlugin.class);

    private final Player player;
    private final String objectiveName = "sidebar";
    private boolean registered = false;
    private final Set<String> shownScores = new HashSet<>();
    private Object objective;

    private static Method getHandleMethod;
    private static Field connectionField;
    private static Method sendMethod;

    private static Constructor<?> objectiveConstructor;
    private static Constructor<?> objectivePacketConstructor;
    private static Constructor<?> displayPacketConstructor;
    private static Constructor<?> scorePacketConstructor;
    private static Constructor<?> resetScorePacketConstructor;

    private static Method literalMethod;
    private static Object sidebarDisplaySlot;
    private static Object integerRenderType;
    private static Object dummyScoreboard;
    private static Object dummyCriteria;
    private static Object blankNumberFormat;

    private static Method setDisplayNameMethod;
    private static Method setRenderTypeMethod;

    private static boolean initialized;

    static {
        try {
            Player dummyPlayer = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            Class<?> craftPlayerClass;

            if (dummyPlayer != null) {
                craftPlayerClass = dummyPlayer.getClass();
            } else {
                String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
                String version = serverPackage.substring(serverPackage.lastIndexOf('.') + 1);
                craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            }

            getHandleMethod = craftPlayerClass.getMethod("getHandle");

            Class<?> serverPlayerClass = Class.forName("net.minecraft.server.level.ServerPlayer");
            connectionField = serverPlayerClass.getField("connection");
            Class<?> connectionClass = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
            sendMethod = connectionClass.getMethod("send", Class.forName("net.minecraft.network.protocol.Packet"));

            Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
            literalMethod = componentClass.getMethod("literal", String.class);

            Class<?> displaySlotClass = Class.forName("net.minecraft.world.scores.DisplaySlot");
            sidebarDisplaySlot = displaySlotClass.getEnumConstants()[1];

            Class<?> renderTypeClass = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
            integerRenderType = renderTypeClass.getEnumConstants()[0];

            Class<?> criteriaClass = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
            Field dummyField = criteriaClass.getField("DUMMY");
            dummyCriteria = dummyField.get(null);

            Class<?> blankFormatClass = Class.forName("net.minecraft.network.chat.numbers.BlankFormat");
            Field instanceField = blankFormatClass.getField("INSTANCE");
            blankNumberFormat = instanceField.get(null);

            Class<?> scoreboardClass = Class.forName("net.minecraft.world.scores.Scoreboard");
            dummyScoreboard = scoreboardClass.getDeclaredConstructor().newInstance();

            Class<?> objectiveClass = Class.forName("net.minecraft.world.scores.Objective");
            objectiveConstructor = objectiveClass.getConstructor(
                scoreboardClass,
                String.class,
                criteriaClass,
                componentClass,
                renderTypeClass,
                boolean.class,
                Class.forName("net.minecraft.network.chat.numbers.NumberFormat")
            );

            setDisplayNameMethod = objectiveClass.getMethod("setDisplayName", componentClass);
            setRenderTypeMethod = objectiveClass.getMethod("setRenderType", renderTypeClass);

            Class<?> objectivePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
            Class<?> displayPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
            Class<?> scorePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
            Class<?> resetScorePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundResetScorePacket");

            objectivePacketConstructor = objectivePacketClass.getConstructor(objectiveClass, int.class);
            displayPacketConstructor = displayPacketClass.getConstructor(displaySlotClass, objectiveClass);
            scorePacketConstructor = scorePacketClass.getConstructor(
                String.class, String.class, int.class, Optional.class, Optional.class
            );
            resetScorePacketConstructor = resetScorePacketClass.getConstructor(String.class, String.class);

            initialized = true;
            plugin.getLogger().info("Successfully initialized scoreboard packet system!");

        } catch (Exception e) {
            DeluxeHubPlugin pluginInstance = JavaPlugin.getPlugin(DeluxeHubPlugin.class);
            pluginInstance.getLogger().log(Level.SEVERE, "Failed to initialize scoreboard NMS reflection", e);
            initialized = false;
        }
    }

    public ScoreHelper(Player player) {
        if (!initialized) {
            throw new IllegalStateException("ScoreHelper NMS reflection failed to initialize");
        }

        this.player = player;
    }

    public void setTitle(String title) {
        title = setPlaceholders(title);
        title = title.length() > 256 ? title.substring(0, 256) : title;

        try {
            if (!registered) {
                createObjective(title);
                registered = true;
            } else {
                updateObjective(title);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set scoreboard title", e);
        }
    }

    public void setSlot(int slot, String text) {
        text = setPlaceholders(text);
        String entry = genEntry(slot);

        try {
            setScore(entry, slot, text);
            shownScores.add(entry);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set scoreboard slot " + slot, e);
        }
    }

    public void removeSlot(int slot) {
        String entry = genEntry(slot);
        if (shownScores.contains(entry)) {
            try {
                resetScore(entry);
                shownScores.remove(entry);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to remove scoreboard slot " + slot, e);
            }
        }
    }

    public void remove() {
        if (!registered) {
            return;
        }

        try {
            Object packet = objectivePacketConstructor.newInstance(objective, 1);
            sendPacket(packet);
            registered = false;
            shownScores.clear();
        } catch (Exception e) {
            // Silently fail during shutdown
        }
    }

    private void createObjective(String title) throws Exception {
        Object titleComponent = createComponent(title);

        objective = objectiveConstructor.newInstance(
            dummyScoreboard,
            objectiveName,
            dummyCriteria,
            titleComponent,
            integerRenderType,
            false,
            blankNumberFormat
        );

        Object packet = objectivePacketConstructor.newInstance(objective, 0);
        sendPacket(packet);

        Object displayPacket = displayPacketConstructor.newInstance(sidebarDisplaySlot, objective);
        sendPacket(displayPacket);
    }

    private void updateObjective(String title) throws Exception {
        Object titleComponent = createComponent(title);

        setDisplayNameMethod.invoke(objective, titleComponent);
        setRenderTypeMethod.invoke(objective, integerRenderType);

        Object packet = objectivePacketConstructor.newInstance(objective, 2);
        sendPacket(packet);
    }

    private void setScore(String holder, int score, String displayText) throws Exception {
        Object displayComponent = displayText.isEmpty() ? null : createComponent(displayText);

        Object packet = scorePacketConstructor.newInstance(
            holder,
            objectiveName,
            score,
            Optional.ofNullable(displayComponent),
            Optional.of(blankNumberFormat)
        );
        sendPacket(packet);
    }

    private void resetScore(String holder) throws Exception {
        Object packet = resetScorePacketConstructor.newInstance(holder, objectiveName);
        sendPacket(packet);
    }

    private Object createComponent(String text) throws Exception {
        return literalMethod.invoke(null, text);
    }

    private void sendPacket(Object packet) throws Exception {
        Object entityPlayer = getHandleMethod.invoke(player);
        Object connection = connectionField.get(entityPlayer);
        sendMethod.invoke(connection, packet);
    }

    public String setPlaceholders(String text) {
        return ColorUtil.color(PlaceholderUtil.setPlaceholders(text, this.player));
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }
}
