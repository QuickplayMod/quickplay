package co.bugg.quickplay.config;

import co.bugg.quickplay.util.GsonPostProcessorFactory;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Abstract configuration for Quickplay
 */
public abstract class AConfiguration implements Serializable {

    /**
     * Pretty-printing GSON instance
     */
    public transient final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    /**
     * Name of the file for this configuration
     */
    public transient String fileName;
    /**
     * File for this configuration
     */
    public transient File file;

    /**
     * Constructor
     * @param fileName name of the file this configuration should save to
     */
    public AConfiguration(String fileName) {
       setFile(new File(AssetFactory.configDirectory + fileName));
    }

    /**
     * Create a backup of the passed file name in the new file location.
     * @param currentFile File name of the current file.
     * @param newLocation File name of the file it should be backed up to.
     * @throws IOException File read/write error
     */
    public static void createBackup(String currentFile, String newLocation) throws IOException {
        File current = new File(AssetFactory.configDirectory + currentFile);
        if(!current.exists()) {
            return;
        }
        final String contents = AConfiguration.getConfigContents(currentFile);

        File newFile = new File(AssetFactory.configDirectory + newLocation);
        newFile.createNewFile();
        Files.write(contents, newFile, StandardCharsets.UTF_8);
    }

    /**
     * Get the contents of the file with the passed file name
     * @param fileName Name of the file to get the contents of
     * @return The contents of the file with the passed name. Null if the file does not exist.
     * @throws IOException File read error
     */
    public static String getConfigContents(String fileName) throws IOException {
        if(fileName == null || fileName.length() <= 0 || fileName.equals(".")) {
            return null;
        }
        File f = new File(AssetFactory.configDirectory + fileName);
        if(!f.exists()) {
            return null;
        }
        return Files.toString(f, StandardCharsets.UTF_8);
    }

    /**
     * Set the file of this configuration
     * @param file File to set
     * @return this
     */
    public AConfiguration setFile(File file) {
        this.fileName = file.toPath().getFileName().toString();
        this.file = file;

        return this;
    }

    /**
     * Save this configuration
     * @return This
     * @throws IOException on a writing error
     */
    public AConfiguration save() throws IOException {
        String contents = GSON.toJson(this);
        Files.write(contents.getBytes(), file);
        return this;
    }

    /**
     * Load a configuration
     * @param name Name of the configuration with the file extension
     * @param type Type of the configuration being loaded
     * @return The configuration loaded
     * @throws IOException Reading error
     * @throws JsonSyntaxException Invalid JSON
     */
    public static AConfiguration load(String name, Class<? extends AConfiguration> type) throws IOException, JsonSyntaxException {
        final File file = new File(AssetFactory.configDirectory + name);

        if(!file.exists()) {
            throw new FileNotFoundException("Configuration file \"" + name + "\" not found.");
        }

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonPostProcessorFactory()).create();
        final String contents = AConfiguration.getConfigContents(name);

        final AConfiguration newConfig = gson.fromJson(contents, type);
        newConfig.setFile(file);

        return newConfig;
    }
}
