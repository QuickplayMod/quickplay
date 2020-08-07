package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.nio.ByteBuffer;

/**
 * ID: 20
 * Received by the server when the client changes locations on Hypixel.
 *
 * Payload Order:
 * Location JSON
 */
public class HypixelLocationChangedAction extends Action {

    public HypixelLocationChangedAction() {}

    /**
     * Create a new HypixelLocationChangedAction.
     * @param locationJson JSON about this player's location
     */
    public HypixelLocationChangedAction(JsonElement locationJson) {
        super();
        this.id = 20;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(locationJson).getBytes()));
    }
}
