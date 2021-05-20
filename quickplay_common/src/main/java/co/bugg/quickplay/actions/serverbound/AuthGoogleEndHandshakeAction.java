package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 29
 * Received by the server when the client is done authenticating with
 * Google's servers, and Quickplay's backend should check for authenticity.
 *
 * Users can authenticate with either Google or Minecraft, but Minecraft UUIDs
 * are the core to identifying a user. As such, a request to authenticate over Google should
 * be entertained initially, but by the end, if the user does not have a link between their
 * Google account and their Minecraft UUID in the database, they should be rejected.
 *
 * @see <a href="https://developers.google.com/identity/sign-in/web/sign-in">Google Sign In</a>
 * @see <a href="https://developers.google.com/identity/sign-in/web/backend-auth">Backend Auth</a>
 *
 * Payload Order:
 * ID token
 */
public class AuthGoogleEndHandshakeAction extends Action {

    public AuthGoogleEndHandshakeAction() {}

    /**
     * Create a new AuthGoogleEndHandshakeAction.
     * @param token The current client's ID token
     */
    public AuthGoogleEndHandshakeAction(String token) {
        super();
        this.id = 29;
        this.addPayload(ByteBuffer.wrap(token.getBytes()));
    }
}
