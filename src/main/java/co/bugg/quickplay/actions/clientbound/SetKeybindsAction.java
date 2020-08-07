package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.QuickplayKeybind;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * ID: 15
 * Set the list keybinds to a new JSON object for this user.
 * This is currently only used to migrate keybinds from pre-2.1.0 to post-2.1.0.
 * @see co.bugg.quickplay.actions.serverbound.MigrateKeybindsAction
 *
 * Payload Order:
 * valid JSON that goes into keybinds.json
 */
public class SetKeybindsAction extends Action {

    public SetKeybindsAction() {}

    /**
     * Create a new SetKeybindsAction.
     * @param keybinds New keybinds to serialize and send to the client.
     */
    public SetKeybindsAction(List<QuickplayKeybind> keybinds) {
        super();
        this.id = 15;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(keybinds).getBytes()));
    }

    @Override
    public void run() {
        final String json = this.getPayloadObjectAsString(0);
        Type listType = new TypeToken<ArrayList<QuickplayKeybind>>(){}.getType();
        Quickplay.INSTANCE.keybinds.keybinds = new Gson().fromJson(json, listType);

    }
}
