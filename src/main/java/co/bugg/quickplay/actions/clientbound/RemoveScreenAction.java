package co.bugg.quickplay.actions.clientbound;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 40
 * Remove a Screen from the client. If the Screen doesn't exist, nothing happens.
 *
 * Payload Order:
 * key
 */
public class RemoveScreenAction extends Action {

    public RemoveScreenAction() {}

    /**
     * Create a new RemoveScreenAction.
     * @param screenKey Key of the Screen to be removed, if it exists.
     */
    public RemoveScreenAction(String screenKey) {
        super();
        this.id = 40;
        this.addPayload(ByteBuffer.wrap(screenKey.getBytes()));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.screenMap.remove(this.getPayloadObjectAsString(0));
    }
}
