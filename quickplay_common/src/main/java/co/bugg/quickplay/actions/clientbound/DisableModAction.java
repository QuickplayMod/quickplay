package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 2
 * Disable the Quickplay mod for the receiving client.
 *
 * Payload Order:
 * Reason
 */
public class DisableModAction extends Action {

    public DisableModAction() {}

    /**
     * Create a new DisableModAction.
     * @param reason The reason the mod was disabled
     */
    public DisableModAction(String reason) {
        super();
        this.id = 2;
        this.addPayload(ByteBuffer.wrap(reason.getBytes()));
    }

    @Override
    public void run() {
        final String reason = this.getPayloadObjectAsString(0);
        Quickplay.INSTANCE.disable(reason);
    }
}
