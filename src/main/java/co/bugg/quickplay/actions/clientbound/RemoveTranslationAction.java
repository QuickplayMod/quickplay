package co.bugg.quickplay.actions.clientbound;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 45
 * Remove a Translation from the client. If the Translation doesn't exist, nothing happens.
 *
 * Payload Order:
 * key
 * lang
 */
public class RemoveTranslationAction extends Action {

    public RemoveTranslationAction() {}
    /**
     * Create a new RemoveTranslationAction.
     * @param key Key of the translation to be removed, if it exists.
     * @param lang Language of the translation to remove.
     */
    public RemoveTranslationAction(String key, String lang) {
        super();
        this.id = 45;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(lang.getBytes()));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.translator
                .remove(this.getPayloadObjectAsString(0), this.getPayloadObjectAsString(1));
    }
}
