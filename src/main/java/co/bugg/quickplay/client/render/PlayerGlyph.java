package co.bugg.quickplay.client.render;

import java.net.URL;
import java.util.UUID;

public class PlayerGlyph {
    public final UUID userUUID;
    public final URL resource;
    public final Double height;

    public PlayerGlyph(UUID uuid, URL resource, Double height) {
        this.userUUID = uuid;
        this.resource = resource;
        this.height = height;
    }
}
