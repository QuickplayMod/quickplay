package co.bugg.quickplay.client.render;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.config.AssetFactory;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Quickplay Glyph class
 */
public class PlayerGlyph {
    /**
     * UUID of the owner
     */
    public final UUID uuid;
    /**
     * URL to the glyph image
     */
    public final URL path;
    /**
     * Height of the glyph
     */
    public final Double height;
    /**
     * Vertical offset from the default position that the glyph should be rendered at
     * TODO
     */
    public final Double yOffset = 0.0;
    /**
     * Whether a download has already been attempted
     * or is currently being attempted on this glyph
     */
    public boolean downloadAttempted = false;

    /**
     * Constructor
     * @param uuid UUID of the owner
     * @param resource URL to the glyph image
     * @param height Height of the glyph
     */
    public PlayerGlyph(UUID uuid, URL resource, Double height) {
        this.uuid = uuid;
        this.path = resource;
        this.height = height;
    }

    /**
     * Try to download this glyph to the Glyphs resource folder
     */
    public synchronized void download() {
        downloadAttempted = true;
        final HttpGet get = new HttpGet(path.toString());

        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) Quickplay.INSTANCE.requestFactory.httpClient.execute(get)) {

            int responseCode = httpResponse.getStatusLine().getStatusCode();

            // If the response code is a successful one & request header is png
            final String contentType = httpResponse.getEntity().getContentType().getValue();
            if (200 <= responseCode && responseCode < 300 && (contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg"))) {

                final File file = new File(AssetFactory.glyphsDirectory + Hashing.md5().hashString(path.toString(), Charset.forName("UTF-8")).toString() + ".png");
                // Try to create file if necessary
                if(!file.exists() && !file.createNewFile())
                    throw new IllegalStateException("Glyph file could not be created.");

                // Write contents
                final InputStream in = httpResponse.getEntity().getContent();
                final FileOutputStream out = new FileOutputStream(file);
                IOUtils.copy(in, out);
                out.close();
                in.close();
            }
            httpResponse.close();

            // Reload the resource pack
            final Field resourceManagerField = Minecraft.class.getDeclaredField("mcResourceManager");
            resourceManagerField.setAccessible(true);
            SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) resourceManagerField.get(Minecraft.getMinecraft());
            resourceManager.reloadResourcePack(Quickplay.INSTANCE.resourcePack);
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
