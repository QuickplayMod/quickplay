package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 4
 * Send a message to the client's system.out. Mainly used for debugging.
 */
public class SystemOutAction extends Action {

    public SystemOutAction() {}

    /**
     * Create a new SystemOutAction.
     * @param message the message to send to the client's logs
     */
    public SystemOutAction(String message) {
        super();
        this.id = 4;
        this.addPayload(ByteBuffer.wrap(message.getBytes()));
    }

    @Override
    public void run() {
        System.out.println(this.getPayloadObjectAsString(0));
    }
}
