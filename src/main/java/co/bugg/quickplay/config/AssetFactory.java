package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class AssetFactory {

    public String rootDirectory = "quickplay/";
    public String configDirectory = rootDirectory + "configs/";
    public String resourcesDirectory = rootDirectory + "resources/";
    public String assetsDirectory = resourcesDirectory + "assets/quickplay/";

    public AConfiguration loadConfig(String name) {
        createDirectories();

        return new ConfigSettings();
    }

    public ResourceLocation loadIcon(URL url) {
        createDirectories();

        return new ResourceLocation("todo");
    }

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
}
