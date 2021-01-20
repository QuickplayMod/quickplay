package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.render.PlayerGlyph;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
        if(Quickplay.INSTANCE.glyphs == null) {
            Quickplay.INSTANCE.glyphs = new ArrayList<>();
        }

        try {
            final UUID uuid = this.getPayloadObjectAsUUID(0);
            final URL url = new URL(this.getPayloadObjectAsString(1));
            final int height = this.getPayloadObject(2).getInt();
            final float yOffset = this.getPayloadObject(3).getFloat();
            final boolean displayInGames = this.getPayloadObject(4).get() != (byte) 0;

            // Copy of glyph list is created to avoid concurrent modification exceptions.
            ArrayList<PlayerGlyph> newGlyphList = new ArrayList<>(Quickplay.INSTANCE.glyphs);
            // Remove glyphs from the list if the glyph belongs to the same user we're adding a glyph for
            for(PlayerGlyph glyph : Quickplay.INSTANCE.glyphs) {
                if(glyph.uuid.equals(uuid)) {
                    newGlyphList.remove(glyph);
                }
            }
            newGlyphList.add(new PlayerGlyph(uuid, url, height, yOffset, displayInGames));
            Quickplay.INSTANCE.glyphs = newGlyphList;
        } catch (MalformedURLException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
