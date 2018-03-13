package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.games.Game;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory for (down)loading & creating mod assets
 */
public class AssetFactory {

    /**
     * Root directory for all things Quickplay
     * Relative to Minecraft root
     */
    public static final String rootDirectory = "quickplay/";
    /**
     * File path to the Quickplay game list cache
     * Relative to Minecraft root
     */
    public static final String gamelistCacheFile = rootDirectory + "cached_gamelist.json";
    /**
     * Directory to all Quickplay configurations
     * Relative to Minecraft root
     */
    public static final String configDirectory = rootDirectory + "configs/";
    /**
     * Directory to all Quickplay resources
     * Relative to Minecraft root
     */
    public static final String resourcesDirectory = rootDirectory + "resources/";
    /**
     * Directory to all Quickplay resource pack assets
     * Relative to Minecraft root
     */
    public static final String assetsDirectory = resourcesDirectory + "assets/quickplay/";
    /**
     * Directory to all Quickplay Glyphs
     * Relative to Minecraft root
     */
    public static final String glyphsDirectory = assetsDirectory + "glyphs/";

    /**
     * Cache life for glyphs in milliseconds
     * 2 days
     */
    public static final long glyphCacheLife = 2L * 24 * 60 * 60 * 1000;
    /**
     * Cache life for game icons in milliseconds
     * 14 days
     */
    public static final long iconCacheLife = 14L * 24 * 60 * 60 * 1000;

    /**
     * Download all icons from the specified URLs
     * @param urls List of URLs to download from
     * @return List of ResourceLocations for all icons
     */
    public List<ResourceLocation> loadIcons(List<URL> urls) {
        createDirectories();

        List<ResourceLocation> resourceLocations = new ArrayList<>();

        for(URL url : urls) {
            File file = getIconFile(url);
            // If the file already exists, no need to download again.
            // If the icon needs to be reset, use REFRESH_CACHE action type.
            if(!file.exists()) {
                System.out.println("Saving file " + file.getPath());
                try {

                    HttpGet get = new HttpGet(url.toURI());

                    CloseableHttpResponse response = (CloseableHttpResponse) Quickplay.INSTANCE.requestFactory.httpClient.execute(get);
                    if(response.getStatusLine().getStatusCode() < 300) {

                        byte[] buffer = new byte[1024];

                        InputStream is = response.getEntity().getContent();
                        OutputStream os = new FileOutputStream(file);

                        for (int length; (length = is.read(buffer)) > 0; ) {
                            os.write(buffer, 0, length);
                        }

                        is.close();
                        os.close();
                        response.close();
                    } else {
                        System.out.println("Can't save file " + file.getPath());
                        continue;
                    }

                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }

            resourceLocations.add(new ResourceLocation(file.getName()));
        }

        try {
            Quickplay.INSTANCE.reloadResourcePack();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
        return resourceLocations;
    }

    /**
     * Dump cached images (glyphs & game icons) that
     * are passed their expiration
     *
     * Game list does not have an expiration
     */
    public void dumpOldCache() {
        final long now = System.currentTimeMillis();

        // Dump glyph cache first
        final File[] glyphFiles = new File(glyphsDirectory).listFiles();
        if(glyphFiles != null) {
            for (final File file : glyphFiles) {
                if(file.exists() && file.isFile() && file.lastModified() + glyphCacheLife < now)
                    file.delete();
            }
        }

        // Dump old icons
        final File[] iconFiles = new File(assetsDirectory).listFiles();
        if(iconFiles != null) {
            for(final File file : iconFiles) {
                if(file.exists() && file.isFile() && file.lastModified() + iconCacheLife < now)
                    file.delete();
            }
        }
    }

    /**
     * Dump all cache, regardless of lifetime
     * This also dumps the cached gamelist.
     */
    public void dumpAllCache() {
        // Dump glyph cache first
        final File[] glyphFiles = new File(glyphsDirectory).listFiles();
        if(glyphFiles != null) {
            for (final File file : glyphFiles) {
                if(file.exists() && file.isFile())
                    file.delete();
            }
        }

        // Dump old icons
        final File[] iconFiles = new File(resourcesDirectory).listFiles();
        if(iconFiles != null) {
            for(final File file : iconFiles) {
                if(file.exists() && file.isFile())
                    file.delete();
            }
        }

        // Delete cached gamelist
        final File gameList = new File(gamelistCacheFile);
        if(gameList.exists() && gameList.isFile())
            gameList.delete();
    }

    /**
     * Create all directories & relevant metadata
     * files for the mod to work properly
     */
    public void createDirectories() {
        final File configDirFile = new File(configDirectory);
        final File resourcesDirFile = new File(resourcesDirectory);
        final File assetsDirFile = new File(assetsDirectory);
        final File glyphsDirFile = new File(glyphsDirectory);

        if(!configDirFile.isDirectory())
            configDirFile.mkdirs();

        if(!resourcesDirFile.isDirectory())
            resourcesDirFile.mkdirs();

        if(!assetsDirFile.isDirectory())
            assetsDirFile.mkdirs();

        if(!glyphsDirFile.isDirectory())
            glyphsDirFile.mkdirs();

        // Create the mcmeta file for the "resource pack"
        final File mcmetaFile = new File(resourcesDirectory + "pack.mcmeta");
        final String mcmetaFileContents = "{\"pack\": {\"pack_format\": 1, \"description\": \"Dynamic mod resources are stored in this pack.\"}}";

        try {
            if (!mcmetaFile.exists())
                mcmetaFile.createNewFile();
            Files.write(mcmetaFile.toPath(), mcmetaFileContents.getBytes());
        } catch(IOException e) {
            System.out.println("Failed to generate mcmeta file! Mod may or may not work properly.");
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }

    /**
     * Register the custom resource pack with Minecraft.
     * The resource pack is used for loading in icons.
     * @return resource pack that is added
     */
    public IResourcePack registerResourcePack() {
        FolderResourcePack resourcePack = new FolderResourcePack(new File(resourcesDirectory));

        // Add the custom resource pack we've created to the list of registered packs
        try {
            Field defaultResourcePacksField;
            try {
                // Try to get the field for the obfuscated "defaultResourcePacks" field
                defaultResourcePacksField = Minecraft.class.getDeclaredField("field_110449_ao");
            } catch(NoSuchFieldException e) {
                // Obfuscated name wasn't found. Let's try the deobfuscated name.
                defaultResourcePacksField = Minecraft.class.getDeclaredField("defaultResourcePacks");
            }

            defaultResourcePacksField.setAccessible(true);
            List<IResourcePack> defaultResourcePacks = (List<IResourcePack>) defaultResourcePacksField.get(Minecraft.getMinecraft());

            defaultResourcePacks.add(resourcePack);

            defaultResourcePacksField.set(Minecraft.getMinecraft(), defaultResourcePacks);

            return resourcePack;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Disabling the mod, as we can't add our custom resource pack.");
            System.out.println("Please report this to @bugfroggy, providing this error log and this list: " + Arrays.toString(Minecraft.class.getDeclaredFields()));
            Quickplay.INSTANCE.disable("Failed to load resources!");
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        return null;
    }

    /**
     * Load the previously cached game list if available,
     * otherwise null is returned.
     * @return List of games or null if unavailable
     */
    public Game[] loadCachedGamelist() throws IOException {
        final File gameListFile = new File(gamelistCacheFile);

        if(!gameListFile.exists() || (!gameListFile.canRead() && !gameListFile.setReadable(true)))
            return null;

        final String contents = new String(Files.readAllBytes(gameListFile.toPath()));
        return Quickplay.organizeGameList(new Gson().fromJson(contents, Game[].class));
    }

    /**
     * Save an array of games to cached "gamelist.json" file.
     * @param gameList List of games
     * @throws IOException Error writing to file
     */
    public void saveCachedGameList(Game[] gameList) throws IOException {
        final File gameListFile = new File(gamelistCacheFile);

        gameList = Quickplay.organizeGameList(gameList);

        // If file doesn't exist and couldn't be created
        if(!gameListFile.exists() && !gameListFile.createNewFile())
            throw new IOException("Failed to create file for cached game list");

        // If file can't be written to and attempts to make it writable failed
        if(!gameListFile.canWrite() && !gameListFile.setWritable(true))
            throw new IOException("Cannot write to file for cached game list");

        final String serializedGameList = new Gson().toJson(gameList);

        Files.write(gameListFile.toPath(), serializedGameList.getBytes());
    }

    /**
     * Get the {@link File} for the provided icon URL
     * URL is md5 hashed and then ".png" is appended
     * @param url URL the icon can be found at
     * @return A new {@link File}
     */
    public File getIconFile(URL url) {
        HashCode hash = Hashing.md5().hashString(url.toString(), Charset.forName("UTF-8"));
        return new File(assetsDirectory + hash.toString() + "." + FilenameUtils.getExtension(url.getPath()));
    }
}
