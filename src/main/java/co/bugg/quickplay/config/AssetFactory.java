package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
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
     * Relative to Minecraft root
     */
    public String rootDirectory = "quickplay/";
    public String configDirectory = rootDirectory + "configs/";
    public String resourcesDirectory = rootDirectory + "resources/";
    public String assetsDirectory = resourcesDirectory + "assets/quickplay/";

    /**
     * TODO Load the mod configuration with provided name
     * @param name Name of the config to load
     * @return Configuration loaded
     */
    public AConfiguration loadConfig(String name) {
        createDirectories();

        return new ConfigSettings();
    }

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
                    String contents = Quickplay.INSTANCE.requestFactory.getContents(url);
                    Files.write(file.toPath(), contents.getBytes());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            resourceLocations.add(new ResourceLocation(file.getName()));
        }

        return resourceLocations;
    }

    /**
     * Create all directories & relevant metadata
     * files for the mod to work properly
     */
    public void createDirectories() {
        final File configDirFile = new File(configDirectory);
        final File resourcesDirFile = new File(resourcesDirectory);
        final File assetsDirFile = new File(assetsDirectory);

        if(!configDirFile.isDirectory())
            configDirFile.mkdirs();

        if(!resourcesDirFile.isDirectory())
            resourcesDirFile.mkdirs();

        if(!assetsDirFile.isDirectory())
            assetsDirFile.mkdirs();

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
        }
    }

    /**
     * Register the custom resource pack with Minecraft.
     * The resource pack is used for loading in icons.
     */
    public void registerResourcePack() {
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Disabling the mod, as we can't add our custom resource pack.");
            System.out.println("Please report this to @bugfroggy, providing this error log and this list: " + Arrays.toString(Minecraft.class.getDeclaredFields()));
            Quickplay.INSTANCE.disable("Failed to load resources!");
            e.printStackTrace();
        }

        // Refresh the resources of the game
        Minecraft.getMinecraft().refreshResources();
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
