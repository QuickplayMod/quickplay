package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.nio.ByteBuffer;

/**
 * ID: 23
 * Received by the server when the client connects to a new Minecraft server.
 * Could be singleplayer, in which case the IP is "singleplayer".
 *
 * Payload Order:
 * The IP of the server joined
 * Metadata JSON about the server
 */
public class ServerJoinedAction extends Action {

    public ServerJoinedAction() {}

    /**
     * Create a new ServerJoinedAction.
     * @param ip The IP of the server the client joined.
     * @param metadata JSON data about the server the client joined, e.g. logo.
     */
    public ServerJoinedAction(String ip, JsonElement metadata) {
        super();
        this.id = 23;
        this.addPayload(ByteBuffer.wrap(ip.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(metadata).getBytes()));
    }
}
