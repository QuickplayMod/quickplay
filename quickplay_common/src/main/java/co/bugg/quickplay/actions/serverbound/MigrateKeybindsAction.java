package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.nio.ByteBuffer;

/**
 * ID: 21
 * Send the list keybinds to the server so the server can respond with a migrated keybinds list.
 * This is currently only used to migrate keybinds from pre-2.1.0 to post-2.1.0.
 * @see co.bugg.quickplay.actions.clientbound.SetKeybindsAction
 *
 * Payload Order:
 * valid JSON that goes into keybinds.json FROM QP 2.0.3 or earlier.
 */
public class MigrateKeybindsAction extends Action {

    public MigrateKeybindsAction() {}

    /**
     * Create a new MigrateKeybindsAction.
     * @param keybinds New keybinds to serialize and send to the server.
     */
    public MigrateKeybindsAction(JsonArray keybinds) {
        super();
        this.id = 21;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(keybinds).getBytes()));
    }

}
