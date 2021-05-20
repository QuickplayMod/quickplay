package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ID: 57
 * Remove a regular expression from the client. If the regex doesn't exist, nothing happens.
 *
 * Payload Order:
 * key
 */
public class RemoveRegexAction extends Action {

    public RemoveRegexAction() {}

    /**
     * Create a new RemoveRegexAction.
     * @param key  Key of the regex to be removed, if it exists.
     */
    public RemoveRegexAction(String key, String value) {
        super();
        this.id = 57;
        this.addPayload(ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void run() {
        String key = this.getPayloadObjectAsString(0);
        Quickplay.INSTANCE.elementController.removeRegularExpression(key);
    }
}
