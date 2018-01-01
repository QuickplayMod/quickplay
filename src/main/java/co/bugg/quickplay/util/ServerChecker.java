package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
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
     * Whether our chat event handler should listen
     * & cancel messages that look like /whereami
     *
     * @see this#onChat(ClientChatReceivedEvent)
     */
    public boolean listenForWhereami = false;

    /**
     * Constructor
     * @param callback Callback to run when checks are complete
     */
    public ServerChecker(ServerCheckerCallback callback) {
        this.callback = callback;

        String ip = getCurrentIP();
        if(!ip.equals("singleplayer")) {
            Pattern hypixelPattern = Pattern.compile("^(?:(?:(?:.\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = hypixelPattern.matcher(ip);

            // If the current IP matches the regex above
            if (matcher.find()) {
                Quickplay.INSTANCE.onHypixel = true;
                this.ip = ip;
                runCallback(true, this.ip, VerificationMethod.IP);
            } else {
                // Not on a recognized IP, let's check the scoreboard
                // for "www.hypixel.net" or if /whereami is a valid command
                // by registering this as an event handler
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
        // Wait one second for the scoreboard to load
        new TickDelay(() -> {
            // Check if the scoreboard contains "www.hypixel.net"
            if(checkScoreboardForHypixel()) {
                runCallback(true, this.ip, VerificationMethod.SCOREBOARD);
            } else {
                // Start listening for the "/whereami" text
                this.listenForWhereami = true;
                // Run /whereami
                runHypixelCommands();
                // If still listening after 5 seconds then
                // assume that the player isn't on Hypixel
                new TickDelay(() -> {
                    if(this.listenForWhereami) {
                        this.listenForWhereami = false;
                        runCallback(false, this.ip, null);
                    }
                }, 100);
            }
        }, 20);
    }

    /**
     * Called when a chat message is recieved
     * @param event Event data
     * @see ClientChatReceivedEvent
     */
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // If we should be looking for /whereami text
        if(listenForWhereami) {
            final String message = event.message.getUnformattedText();
            // If the message matches what is normally sent when a player does /whereami
            if(message.startsWith("You are currently in limbo") || message.startsWith("You are currently connected to server ")) {
                event.setCanceled(true);
                this.listenForWhereami = false;
                runCallback(true, this.ip, VerificationMethod.COMMAND);
            }
        }
    }

    /**
     * Send chat commands (i.e. /whereami) to determine
     * if the player is on Hypixel
     */
    public void runHypixelCommands() {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        player.sendChatMessage("/whereami");
    }

    /**
     * Check if the scoreboard contains "www.hypixel.net"
     * @return Whether the scoreboard contains "www.hypixel.net", or false if no scoreboard
     */
    public boolean checkScoreboardForHypixel() {
        List<String> list = ScoreboardUtils.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());

        if(list != null && list.get(0) != null)
            return list.get(0).contains("www.hypixel.net");
        else
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
        SCOREBOARD,
        COMMAND
    }
}
