package co.bugg.quickplay.actions.clientbound;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 42
 * Remove an Aliased Action from the client. If the Aliased Action doesn't exist, nothing happens.
 *
 * Payload Order:
 * key
 */
public class RemoveAliasedActionAction extends Action {

    public RemoveAliasedActionAction() {}

    /**
     * Create a new RemoveAliasedActionAction.
     * @param aliasedActionKey Key of the AliasedAction to be removed, if it exists.
     */
    public RemoveAliasedActionAction(String aliasedActionKey) {
        super();
        this.id = 42;
        this.addPayload(ByteBuffer.wrap(aliasedActionKey.getBytes()));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.elementController.removeAliasedAction(this.getPayloadObjectAsString(0));
    }
}
