package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Location;
import com.google.gson.Gson;

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
     * @param location JSON about this player's location
     */
    public HypixelLocationChangedAction(Location location) {
        super();
        this.id = 20;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(location).getBytes()));
    }
}
