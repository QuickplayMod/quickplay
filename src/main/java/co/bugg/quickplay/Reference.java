package co.bugg.quickplay;

import net.minecraft.launchwrapper.Launch;

/**
 * Static reference objects about this Minecraft mod
 */
public class Reference {
    /**
     * ID for this Minecraft Forge mod
     */
    public static final String MOD_ID = "quickplay";
    /**
     * Display name for this Forge mod
     */
    public static final String MOD_NAME = "Quickplay";
    /**
     * Version of this forge mod
     */
    public static final String VERSION = "2.1.0-beta5-alpha5";
    /**
     * Google Analytics tracking ID
     */
    public static final String ANALYTICS_TRACKING_ID = "UA-60675209-4";
    /**
     * URI pointing to the backend's socket
     */
    public static final String BACKEND_SOCKET_URI = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ?
            "ws://localhost:54678/" : "wss://qp-socket.bugg.co/";
}
