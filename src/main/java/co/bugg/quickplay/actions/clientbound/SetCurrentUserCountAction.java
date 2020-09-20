package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 36
 *
 * Set the current number of clients connected to the Quickplay backend.
 * This should be a protected Action, only sent to Admins.
 *
 * Payload Order:
 * client count
 */
public class SetCurrentUserCountAction extends Action {

    public SetCurrentUserCountAction() {}

    /**
     * Create a new SetCurrentUserCountAction.
     * @param count The total number of connected clients at the time of this Action being sent.
     */
    public SetCurrentUserCountAction(int count) {
        super();
        this.id = 36;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(count);
        buf.rewind();
        this.addPayload(buf);
    }

    @Override
    public void run() {
        // Currently only used in the web panel.
    }
}
