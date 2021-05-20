package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for checking what server the client is
 * currently connected to, specifically if it's
 * the Hypixel Network
 */
public class ServerChecker {
    /**
     * Callback to run when the checks are complete
     */
    public ServerCheckerCallback callback;
    /**
     * IP the client is connected to
     */
    public String ip = "unknown";

    /**
     * Constructor
     * @param callback Callback to run when checks are complete
     */
    public ServerChecker(ServerCheckerCallback callback) {
        this.callback = callback;

        final String ip = getCurrentIP();
        if(ip != null && !ip.equals("singleplayer")) {
            Pattern hypixelPattern = Pattern.compile("^(?:(?:(?:.*\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = hypixelPattern.matcher(ip);

            // If the current IP matches the regex above
            if (matcher.find()) {
                this.ip = ip;
                runCallback(this.ip);
            } else {
                // Not on a recognized IP, let's check server metadata, which
                // occurs on world load
                Quickplay.INSTANCE.registerEventHandler(this);
            }
        } else {
            runCallback(this.ip = "singleplayer");
        }
    }

    /**
     * Called when a Minecraft world is loaded
     * @param event Event data
     * @see WorldEvent.Load
     */
    @SubscribeEvent
    public void onJoinWorld(WorldEvent.Load event) {
        // Only one world load is necessary
        Quickplay.INSTANCE.unregisterEventHandler(this);
        // Wait one second for everything to load properly
        new TickDelay(() -> {
            runCallback(this.ip);
        }, 20);
    }

    /**
     * Checks whether the tab list header & footer contain information that would point to the current server being Hypixel.
     * @param tabOverlay GUI overlay for the tab list
     * @param fieldName name of the field to check (either <code>header</code> or <code>footer</code>)
     * @param srgName field name in an obfuscated environment
     * @return Whether the field <code>fieldName</code> in the object <code>tabOverlay</code> contains "hypixel.net"
     * @throws NoSuchFieldException The field couldn't be found
     * @throws IllegalAccessException The field couldn't be accessed
     */
    public boolean checkTabField(GuiPlayerTabOverlay tabOverlay, String fieldName, String srgName) throws
            NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Field headerField;
        try {
            // Try deobfuscated
            headerField = tabOverlay.getClass().getDeclaredField(fieldName);
        } catch(NoSuchFieldException e) {
            // Assume SRG otherwise
            headerField = tabOverlay.getClass().getDeclaredField(srgName);
        }

        if(headerField != null) {
            headerField.setAccessible(true);

            // OrangeMarshall's Vanilla Enhancements conflicts with this mod. He has a field called
            // FieldWrapper which wraps IChatComponent, and you must call .get() on the wrapper as well
            // i.e. instead of headerField.get(tabOverlay), headerField.get(tabOverlay).get(tabOverlay).

            final Object headerObj = headerField.get(tabOverlay);
            IChatComponent component;
            // If user is using Vanilla Enhancements
            if(Loader.instance().getModList().stream().anyMatch(mod -> mod.getName().equals("Vanilla Enhancements"))) {
                final String type = "com.orangemarshall.enhancements.util.FieldWrapper";
                final Class<?> clazz = Class.forName(type);

                // Cast to FieldWrapper, then get the method "get" taking one parameger Object obj
                final Method fieldWrapperGetMethod = clazz.cast(headerObj).getClass().getDeclaredMethod("get", Object.class);
                fieldWrapperGetMethod.setAccessible(true);

                // Execute the method on headerObj, passing tabOverlay as parameter obj
                component = (IChatComponent) fieldWrapperGetMethod.invoke(headerObj, tabOverlay);
            } else {
                component = (IChatComponent) headerObj;
            }

            return component != null && component.getUnformattedText() != null && component.getUnformattedText().toLowerCase().contains("hypixel.net");
        }

        return false;
    }

    /**
     * Run the callback & unregister this as an event listener
     * @param ip IP the user is connected to
     */
    public void runCallback(String ip) {
        callback.run(ip);
        Quickplay.INSTANCE.unregisterEventHandler(this);
    }

    /**
     * Gets the current IP the client is connected to
     * @return The IP the client is currently connected to
     */
    public static String getCurrentIP() {
        String ip;
        if(Minecraft.getMinecraft().isSingleplayer()) {
            ip = "singleplayer";
        } else {
            ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
            ip = (serverData == null) ? null : serverData.serverIP;
        }

        return ip;
    }
}
