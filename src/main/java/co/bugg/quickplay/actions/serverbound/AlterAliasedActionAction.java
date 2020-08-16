package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.AliasedAction;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 35
 * Order for the server to alter an AliasedAction in it's database. If the AliasedAction does not exist, it is created.
 * If the AliasedAction passed does not exist and is missing fields, the default is used. If no default is available,
 * then an error message is sent to the client.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * AliasedAction key
 * JSON serialized AliasedAction
 */
public class AlterAliasedActionAction extends Action {

    public AlterAliasedActionAction() {}

    /**
     * Create a new AlterAliasedActionAction.
     * @param key Key of the item the client is requesting to be altered.
     * @param newAliasedAction The new AliasedAction to replace at the key. Should not replace values
     * which are undefined.
     */
    public AlterAliasedActionAction(String key, AliasedAction newAliasedAction) {
        super();
        this.id = 35;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(newAliasedAction).getBytes()));
    }
}
