package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.render.PlayerGlyph;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 46
 * Received by the server when the client requests to modify their Quickplay glyph.
 * Should be restricted to Premium users.
 *
 * Payload Order:
 * Glyph JSON
 */
public class AlterGlyphAction extends Action {

    /**
     * Create a new AlterGlyphAction.
     * @param newGlyph The new Glyph for the user. Should not replace values which are undefined.
     */
    public AlterGlyphAction(PlayerGlyph newGlyph) {
        super();
        this.id = 46;

        this.addPayload(ByteBuffer.wrap(new Gson().toJson(newGlyph).getBytes()));
    }
}
