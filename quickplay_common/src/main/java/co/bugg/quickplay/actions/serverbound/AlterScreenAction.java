package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.elements.Screen;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 33
 * Order for the server to alter a Screen in it's database. If the Screen does not exist, it is created.
 * If the Screen passed does not exist and is missing fields, the default is used. If no default is available,
 * then an error message is sent to the client.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Screen key
 * JSON serialized Screen
 */
public class AlterScreenAction extends Action {

    public AlterScreenAction() {}

    /**
     * Create a new AlterScreenAction.
     * @param key Key of the item the client is requesting to be altered.
     * @param newScreen The new Screen to replace at the key. Should not replace values which are undefined.
     */
    public AlterScreenAction(String key, Screen newScreen) {
        super();
        this.id = 33;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(newScreen).getBytes()));
    }
}
