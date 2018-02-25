package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
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
        if(!ip.equals("singleplayer")) {
            Pattern hypixelPattern = Pattern.compile("^(?:(?:(?:.\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = hypixelPattern.matcher(ip);

            // If the current IP matches the regex above
            if (matcher.find()) {
                Quickplay.INSTANCE.onHypixel = true;
                this.ip = ip;
                runCallback(true, this.ip, VerificationMethod.IP);
            } else {
                // Not on a recognized IP, let's check server metadata, which
                // occurs on world load
                Quickplay.INSTANCE.registerEventHandler(this);
            }
        } else {
            runCallback(false, this.ip = "singleplayer", null);
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
            // Check the server's metadata
            final VerificationMethod metadataVerification = checkServerMetadataForHypixel();
            if(metadataVerification != null) {
                runCallback(true, this.ip, metadataVerification);
            } else {
                callback.run(false, this.ip, null);
            }
        }, 20);
    }

    /**
     * Check if any of the server's metadata contains anything that might point to this server being Hypixel.
     * If the tablist header contains "hypixel.net" then the server is verified.
     * If the tablist fooder contains "hypixel.net" then the server is verified.
     * If the server MOTD contains "hypixel network" then the server is verified.
     * If the server favicon base64 matches Hypixel's logo then the server is verified.
     * @return Which of the above checks were true, or null otherwise.
     */
    public VerificationMethod checkServerMetadataForHypixel() {

        // First check tab list, if it contains any references to Hypixel in the header & footer.
        final GuiPlayerTabOverlay tab = Minecraft.getMinecraft().ingameGUI.getTabList();
        try {
            if(checkTabField(tab, "header"))
                return VerificationMethod.HEADER;
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        try {
            if(checkTabField(tab, "footer"))
                return VerificationMethod.FOOTER;
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        // Next check server MOTD
        final String motd = Minecraft.getMinecraft().getCurrentServerData().serverMOTD;
        if(motd != null && motd.toLowerCase().contains("hypixel network")) {
            return VerificationMethod.MOTD;
        }

        try {
            // Next check server favicon
            final String faviconBase64 = Minecraft.getMinecraft().getCurrentServerData().getBase64EncodedIconData();
            if(faviconBase64 != null) {
                final String hypixelBase64 = Base64.encodeBase64String(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("HypixelMCLogo.png")));
                if(faviconBase64.equals(hypixelBase64))
                    return VerificationMethod.FAVICON;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        // Return null if none of these conditions matched
        return null;
    }

    /**
     * Checks whether the tab list header & footer contain information that would point to the current server being Hypixel.
     * @param tabOverlay GUI overlay for the tab list
     * @param fieldName name of the field to check (either <code>header</code> or <code>footer</code>)
     * @return Whether the field <code>fieldName</code> in the object <code>tabOverlay</code> contains "hypixel.net"
     * @throws NoSuchFieldException The field couldn't be found
     * @throws IllegalAccessException The field couldn't be accessed
     */
    public boolean checkTabField(GuiPlayerTabOverlay tabOverlay, String fieldName) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        final Field headerField = tabOverlay.getClass().getDeclaredField(fieldName);
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
     * @param onHypixel whether online Hypixel
     * @param ip IP the user is connected to
     */
    public void runCallback(boolean onHypixel, String ip, VerificationMethod method) {
        callback.run(onHypixel, ip, method);
        Quickplay.INSTANCE.unregisterEventHandler(this);
    }

    /**
     * Gets the current IP the client is connected to
     * @return The IP the client is currently connected to
     */
    public static String getCurrentIP() {
        String ip;
        if(Minecraft.getMinecraft().isSingleplayer())
            ip = "singleplayer";
        else {
            ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
            ip = (serverData == null) ? "unknown/null" : serverData.serverIP;
        }

        return ip;
    }

    /**
     * Enum for possible ways that the client was verified to be on Hypixel
     */
    public enum VerificationMethod {
        IP,
        HEADER,
        FOOTER,
        MOTD,
        FAVICON
    }
}
