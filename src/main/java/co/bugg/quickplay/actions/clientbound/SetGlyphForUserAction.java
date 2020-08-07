package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * ID: 14
 * Set the URL for a user's Glyph. The client will download this image if it is necessary.
 *
 * Payload Order:
 * user UUID
 * glyph URL
 * glyph height (32intBE)
 * glyph offset (floatBE)
 * glyph in-game visibility (1byte, 1 or 0 for boolean)
 */
public class SetGlyphForUserAction extends Action {

    public SetGlyphForUserAction() {}

    /**
     * Create a new SetGlyphForUserAction.
     * @param uuid The UUID of the user for which this Glyph belongs.
     * @param url The URL of the Glyph.
     * @param height The height of this glyph
     * @param yOffset The offset from the top of the player of this glyph
     * @param displayInGames Whether this glyph should be displayed in games or not.
     */
    public SetGlyphForUserAction(UUID uuid, String url, int height, float yOffset, boolean displayInGames) {
        super();
        this.id = 14;
        this.addPayload(ByteBuffer.wrap(uuid.toString().getBytes()));
        this.addPayload(ByteBuffer.wrap(url.getBytes()));
        this.addPayload(ByteBuffer.allocate(4).putInt(height));
        this.addPayload(ByteBuffer.allocate(8).putFloat(yOffset));
        this.addPayload(ByteBuffer.allocate(1).putInt(displayInGames ? 1 : 0));
    }


    @Override
    public void run() {
        // TODO
    }
}
