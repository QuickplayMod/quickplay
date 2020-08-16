package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 38
 * Received by the server when the client believes it already has an authenticated session, but the connection was
 * lost.
 *
 * Payload Order:
 * Session token
 */
public class AuthReestablishAuthedConnectionAction extends Action {

    public AuthReestablishAuthedConnectionAction() {}

    /**
     * Create a new AuthReestablishAuthedConnectionAction.
     * @param token The session token the client has.
     */
    public AuthReestablishAuthedConnectionAction(String token) {
        super();
        this.id = 38;
        this.addPayload(ByteBuffer.wrap(token.getBytes()));
    }
}
