package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 43
 * Order for the server to alter a translation in it's database. If the translation does not exist, it is created.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Translation key
 * Translation language (lower case)
 * Translation value
 */
public class AlterTranslationAction extends Action {

    public AlterTranslationAction() {}

    /**
     * Create a new AlterTranslationAction.
     * @param key Key of the item the client is requesting to be altered.
     * @param lang Language of the item to be altered.
     * @param value Value to set the key/language pair to.
     */
    public AlterTranslationAction(String key, String lang, String value) {
        super();
        this.id = 43;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(lang.getBytes()));
        this.addPayload(ByteBuffer.wrap(value.getBytes()));
    }
}
