package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 32
 * Order for the server to remove an AliasedAction from it's database.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * AliasedAction key
 */
public class DeleteAliasedActionAction extends Action {

    public DeleteAliasedActionAction() {}

    /**
     * Create a new DeleteAliasedActionAction.
     * @param key Key of the item the client is requesting to be deleted.
     */
    public DeleteAliasedActionAction(String key) {
        super();
        this.id = 32;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
    }
}
