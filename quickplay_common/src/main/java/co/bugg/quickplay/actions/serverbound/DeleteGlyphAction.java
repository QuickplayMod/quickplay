package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * ID: 52
 * Received by the server when the client requests to delete a Glyph. Admins should be able to delete anyone's glyph,
 * but non-admins should only be able to delete their own.
 *
 * Payload Order:
 * UUID of the glyph's owner account
 */
public class DeleteGlyphAction extends Action {

    public DeleteGlyphAction() {}

    /**
     * Create a new DeleteGlyphAction.
     * @param uuid UUID of the owner who's glyph should be removed. Admins should be able to delete anyone's glyph,
     * but non-admins should only be able to delete their own.
     */
    public DeleteGlyphAction(UUID uuid) {
        super();
        this.id = 52;
        this.addPayload(ByteBuffer.wrap(uuid.toString().getBytes()));
    }
}
