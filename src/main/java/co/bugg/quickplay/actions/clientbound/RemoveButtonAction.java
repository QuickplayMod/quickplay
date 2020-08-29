package co.bugg.quickplay.actions.clientbound;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 41
 * Remove a Screen from the client. If the Screen doesn't exist, nothing happens.
 *
 * Payload Order:
 * key
 */
public class RemoveButtonAction extends Action {

    public RemoveButtonAction() {}

    /**
     * Create a new RemoveButtonAction.
     * @param buttonKey Key of the Button to be removed, if it exists.
     */
    public RemoveButtonAction(String buttonKey) {
        super();
        this.id = 41;
        this.addPayload(ByteBuffer.wrap(buttonKey.getBytes()));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.buttonMap.remove(this.getPayloadObjectAsString(0));
    }
}
