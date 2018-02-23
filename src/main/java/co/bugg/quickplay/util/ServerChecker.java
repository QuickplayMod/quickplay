package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Field;
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
            Pattern hypixelPattern = Pattern.compile("^(?:(?:(?:.*\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$", Pattern.CASE_INSENSITIVE);
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
     * @return Whether the tab list contains "MC.HYPIXEL.NET"
     */
    public VerificationMethod checkServerMetadataForHypixel() {

        // First check tab list, if it contains any references to Hypixel in the header & footer.
        // This is best option because header & footer are both constant and present everywhere on Hypixel, including Limbo
        final GuiPlayerTabOverlay tab = Minecraft.getMinecraft().ingameGUI.getTabList();
        try {
            final Field headerField = tab.getClass().getDeclaredField("header");
            if(headerField != null) {
                headerField.setAccessible(true);
                if(((IChatComponent) headerField.get(tab)).getUnformattedText().toLowerCase().contains("hypixel.net"))
                    return VerificationMethod.HEADER;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        try {
            final Field footerField = tab.getClass().getDeclaredField("footer");
            if(footerField != null) {
                footerField.setAccessible(true);
                if(((IChatComponent) footerField.get(tab)).getUnformattedText().toLowerCase().contains("hypixel.net"))
                    return VerificationMethod.FOOTER;
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
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
            final String hypixelBase64 = Base64.encodeBase64String(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("HypixelMCLogo.png")));
            if(faviconBase64.equals(hypixelBase64))
                return VerificationMethod.FAVICON;
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        // Return null if none of these conditions matched
        return null;
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
