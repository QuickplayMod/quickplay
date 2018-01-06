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

public abstract class AConfiguration implements Serializable {

    public transient final Gson GSON = new Gson();
    public transient String fileName;
    public transient File file;

    /**
     * Constructor
     * @param fileName name of the file this configuration should save to
     */
    public AConfiguration(String fileName) {
       setFile(new File(AssetFactory.configDirectory + fileName));
    }

    public AConfiguration setFile(File file) {
        this.fileName = file.toPath().getFileName().toString();
        this.file = file;

        return this;
    }

    public AConfiguration save() throws IOException {
        String contents = GSON.toJson(this);
        Files.write(contents.getBytes(), file);
        return this;
    }

    public static AConfiguration load(String name, Class<? extends AConfiguration> type) throws IOException, JsonSyntaxException {
        final File file = new File(AssetFactory.configDirectory + name);

        if(!file.exists()) {
            throw new FileNotFoundException("Settings file not found");
        }

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonPostProcessorFactory()).create();
        final String contents = Files.toString(file, Charset.forName("UTF-8"));

        final AConfiguration newConfig =  gson.fromJson(contents, type);
        newConfig.setFile(file);

        return newConfig;
    }
}
