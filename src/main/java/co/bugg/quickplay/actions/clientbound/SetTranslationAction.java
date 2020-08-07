package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 17
 * Set the translation value of a specified key in a specified language.
 *
 * Payload Order:
 * key
 * language
 * value
 */
public class SetTranslationAction extends Action {

    public SetTranslationAction() {}

    /**
     * Create a new SetTranslationAction.
     * @param key The key of the translation to set.
     * @param lang The language to set the key for.
     * @param val The value to set the key to.
     */
    public SetTranslationAction(String key, String lang, String val) {
        super();
        this.id = 17;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(lang.getBytes()));
        this.addPayload(ByteBuffer.wrap(val.getBytes()));
    }

    @Override
    public void run() {
        final String key = this.getPayloadObjectAsString(0);
        final String lang = this.getPayloadObjectAsString(1);
        final String val = this.getPayloadObjectAsString(2);

        Quickplay.INSTANCE.translator.set(key, lang, val);
    }
}
