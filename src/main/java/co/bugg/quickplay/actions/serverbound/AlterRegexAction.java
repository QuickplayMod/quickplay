package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 55
 * Order for the server to alter a regular expression in it's database. If the regex does not exist, it is created.
 * This should be a protected action, only allowed for admins.
 *
 * Payload Order:
 * Regex key
 * Regular expression
 */
public class AlterRegexAction extends Action {

    public AlterRegexAction() {}

    /**
     * Create a new AlterRegexAction.
     * @param key Key of the item the client is requesting to be altered.
     * @param value Value to associate with the provided key.
     */
    public AlterRegexAction(String key, String value) {
        super();
        this.id = 55;
        this.addPayload(ByteBuffer.wrap(key.getBytes()));
        this.addPayload(ByteBuffer.wrap(value.getBytes()));
    }
}
