package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 13
 * Notify the client that it's changed to a recognized (or unrecognized) server by Quickplay.
 * If server is not recognized or client is not connected to a server, "unknown" should be sent.
 *
 * Quickplay supports multiple servers. When the client reports a serverbound ServerJoinedAction, the server will
 * respond with this, saying what server it thinks the client is currently on, based on the information provided by the client.
 * This will correspond to actions, screens, etc. and what servers they are available on.
 *
 * Payload Order:
 * server name
 */
public class SetCurrentServerAction extends Action {

    public SetCurrentServerAction() {}

    /**
     * Create a new SetCurrentServerAction.
     * @param serverName The name of the server that the client has connected to.
     */
    public SetCurrentServerAction(String serverName) {
        super();
        this.id = 13;
        this.addPayload(ByteBuffer.wrap(serverName.getBytes()));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.currentServer = this.getPayloadObjectAsString(0);
    }
}
