package co.bugg.quickplay.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Class for checking what server the client is
 * currently connected to, specifically if it's
 * the Hypixel Network
 */
public class ServerChecker {
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
}
