package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 27
 * Received by the server when the client is done authenticating with
 * Mojang's servers, and Quickplay's backend should check for authenticity.
 *
 * Payload Order:
 * Minecraft username
 */
public class AuthMojangEndHandshakeAction extends Action {

    public AuthMojangEndHandshakeAction() {}

    /**
     * Create a new AuthMojangEndHandshakeAction.
     * @param username The current client's username
     */
    public AuthMojangEndHandshakeAction(String username) {
        super();
        this.id = 27;
        this.addPayload(ByteBuffer.wrap(username.getBytes()));
    }
}
