package co.bugg.quickplay.actions.clientbound;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.render.PlayerGlyph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ID: 53
 * Remove a specified user's glyph from the stored list of glyphs.
 *
 * Payload Order:
 * UUID
 */
public class RemoveGlyphAction extends Action {

    public RemoveGlyphAction() {}

    /**
     * Create a new RemoveGlyphAction.
     * @param uuid UUID of the account whose glyph should be removed.
     */
    public RemoveGlyphAction(UUID uuid) {
        super();
        this.id = 53;
        this.addPayload(ByteBuffer.wrap(uuid.toString().getBytes()));
    }

    @Override
    public void run() {
        UUID givenUuid = this.getPayloadObjectAsUUID(0);

        List<PlayerGlyph> newGlyphList = new ArrayList<>(Quickplay.INSTANCE.glyphs);
        for(PlayerGlyph glyph : Quickplay.INSTANCE.glyphs) {
            if(glyph != null && glyph.uuid.equals(givenUuid)) {
                newGlyphList.remove(glyph);
            }
        }
        Quickplay.INSTANCE.glyphs = newGlyphList;
    }
}
