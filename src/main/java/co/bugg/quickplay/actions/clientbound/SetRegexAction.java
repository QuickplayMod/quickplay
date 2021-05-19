package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.RegularExpression;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ID: 54
 * Set a regex in the client with the provided key and value.
 *
 * Payload Order:
 * key
 * value
 */
public class SetRegexAction extends Action {

    public SetRegexAction() {}

    /**
     * Create a new SetRegexAction.
     * @param key the key to save the regex under.
     * @param value The regular expression to save.
     */
    public SetRegexAction(String key, String value) {
        super();
        this.id = 54;
        this.addPayload(ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8)));
        this.addPayload(ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void run() {
        final String key = this.getPayloadObjectAsString(0);
        final String value = this.getPayloadObjectAsString(1);
        final RegularExpression regex = new RegularExpression(key, value);
        Quickplay.INSTANCE.elementController.putElement(regex);
    }
}
