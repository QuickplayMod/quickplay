package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.elements.Button;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 34
 * Order for the server to alter a Button in it's database. If the Button does not exist, it is created.
 * If the Button passed does not exist and is missing fields, the default is used. If no default is available,
 * then an error message is sent to the client.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Button key
 * JSON serialized Button
 */
public class AlterButtonAction extends Action {

    public AlterButtonAction() {}

    /**
     * Create a new AlterButtonAction.
     * @param key Key of the item the client is requesting to be altered.
     * @param newButton The new Button to replace at the key. Should not replace values which are undefined.
     */
    public AlterButtonAction(String key, Button newButton) {
        super();
        this.id = 34;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(newButton).getBytes()));
    }
}
