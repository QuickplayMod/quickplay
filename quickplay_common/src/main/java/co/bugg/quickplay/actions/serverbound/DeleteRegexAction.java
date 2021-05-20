package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 56
 * Order for the server to remove a regex from it's database.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Regex key
 */
public class DeleteRegexAction extends Action {

    public DeleteRegexAction() {}

    /**
     * Create a new DeleteRegexAction.
     * @param key Key of the item the client is requesting to be deleted.
     */
    public DeleteRegexAction(String key) {
        super();
        this.id = 56;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
    }
}
