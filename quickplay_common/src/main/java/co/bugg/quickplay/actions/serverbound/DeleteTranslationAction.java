package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 44
 * Order for the server to remove a translation from it's database.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Translation key
 * Translation lang
 */
public class DeleteTranslationAction extends Action {

    public DeleteTranslationAction() {}

    /**
     * Create a new DeleteTranslationAction.
     * @param key Key of the item the client is requesting to be deleted.
     * @param lang Language of the item the client is requesting to be deleted.
     */
    public DeleteTranslationAction(String key, String lang) {
        super();
        this.id = 44;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(lang.getBytes()));
    }
}
