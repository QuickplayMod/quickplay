package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 30
 * Order for the server to remove a Screen from it's database.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Screen key
 */
public class DeleteScreenAction extends Action {

    public DeleteScreenAction() {}

    /**
     * Create a new DeleteScreenAction.
     * @param key Key of the item the client is requesting to be deleted
     */
    public DeleteScreenAction(String key) {
        super();
        this.id = 30;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
    }
}
