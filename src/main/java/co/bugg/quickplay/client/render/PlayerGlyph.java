package co.bugg.quickplay.client.render;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.config.AssetFactory;
import com.google.common.hash.Hashing;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Quickplay Glyph class
 */
public class PlayerGlyph {
    /**
     * The maximum amount of times to try downloading before giving up
     */
    public static final int maxDownloadAttempts = 5;

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
    public Double height = 20.0;
    /**
     * Vertical offset from the default position that the glyph should be rendered at
     */
    public Double yOffset = 0.0;
    /**
     * Whether this glyph should be dipslayed in-game
     */
    public boolean displayInGames = false;
    /**
     * Whether this Glyph is currently being downloaded or not
     */
    public boolean downloading = false;
    /**
     * The number of times a download has been attempted on this Glyph.
     */
    public int downloadCount = 0;

    /**
     * Constructor
     * @param uuid UUID of the owner
     * @param resource URL to the glyph image
     */
    public PlayerGlyph(UUID uuid, URL resource) {
        this.uuid = uuid;
        this.path = resource;
    }

    /**
     * Try to download this glyph to the Glyphs resource folder
     */
    public synchronized void download() {
        if(!downloading && downloadCount < maxDownloadAttempts) {
            downloading = true;
            downloadCount++;

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

                    // Reload the resource
                    QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                        Quickplay.INSTANCE.reloadResource(file, new ResourceLocation(Reference.MOD_ID, "glyphs/" + file.getName()));
                        downloading = false;
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                downloading = false;
            }
        }
    }
}
