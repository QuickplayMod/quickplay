package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 22
 * Received by the server when the client changes languages.
 *
 * Payload Order:
 * New language ID
 */
public class LanguageChangedAction extends Action {

    public LanguageChangedAction() {}

    /**
     * Create a new LanguageChangedAction.
     * @param langId New language ID
     */
    public LanguageChangedAction(String langId) {
        super();
        this.id = 22;
        this.addPayload(ByteBuffer.wrap(langId.getBytes()));
    }
}
