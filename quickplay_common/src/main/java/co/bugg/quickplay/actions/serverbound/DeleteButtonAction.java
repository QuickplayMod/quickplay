package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 31
 * Order for the server to remove a Button from it's database.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Button key
 */
public class DeleteButtonAction extends Action {

    public DeleteButtonAction() {}

    /**
     * Create a new DeleteButtonAction.
     * @param key Key of the item the client is requesting to be deleted.
     */
    public DeleteButtonAction(String key) {
        super();
        this.id = 31;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
    }
}
