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
import java.nio.charset.Charset;

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
     *
     * @param fileName name of the file this configuration should save to
     */
    public AConfiguration(String fileName) {
        setFile(new File(AssetFactory.configDirectory + fileName));
    }

    /**
     * Set the file of this configuration
     *
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
     *
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
     *
     * @param name Name of the configuration with the file extension
     * @param type Type of the configuration being loaded
     * @return The configuration loaded
     * @throws IOException         Reading error
     * @throws JsonSyntaxException Invalid JSON
     */
    public static AConfiguration load(String name, Class<? extends AConfiguration> type) throws IOException, JsonSyntaxException {
        final File file = new File(AssetFactory.configDirectory + name);

        if (!file.exists()) {
            throw new FileNotFoundException("Configuration file \"" + name + "\" not found.");
        }

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonPostProcessorFactory()).create();
        final String contents = Files.toString(file, Charset.forName("UTF-8"));

        final AConfiguration newConfig = gson.fromJson(contents, type);
        newConfig.setFile(file);

        return newConfig;
    }
}
