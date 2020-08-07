package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.game.QuickplayGuiMainMenu;
import com.google.common.io.Files;
import com.google.gson.*;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Default configuration for storing Quickplay keybinds
 */
public class ConfigKeybinds extends AConfiguration {

    /**
     * Constructor
     */
    public ConfigKeybinds() {
        super("keybinds.json");
    }

    /**
     * Constructor
     *
     * @param addDefaultKeybinds Whether default keybinds should be added or not
     */
    public ConfigKeybinds(boolean addDefaultKeybinds) {
        this();
        if(addDefaultKeybinds) {
            keybinds.add(new QuickplayKeybind(Quickplay.INSTANCE.translator.get("quickplay.config.keybinds.openmain"),
                    Keyboard.KEY_R, QuickplayGuiMainMenu.class));
        }
    }

    /**
     * Check for the need for a conversion of keybinds from pre-2.1.0 to post-2.1.0 format.
     *
     * The logic is handled on the backend, since the backend has a full list of keys while the client doesn't necessarily.
     * A request is sent to the server with {@link co.bugg.quickplay.actions.serverbound.MigrateKeybindsAction}
     *
     * A conversion is assumed to be required ONLY if the "name" field is present on any keybind.
     * Other malformations do not trigger a conversion.
     *
     * @param fileName Name of the file to convert
     * @return True if a conversion is necessary, false otherwise.
     */
    public static boolean checkForConversionNeeded(String fileName) {
        final File f = new File(AssetFactory.configDirectory + fileName);
        if(!f.exists()) { // If the file doesn't exist, no need to convert it...
            return true;
        }

        try {
            final String contents = Files.toString(f, StandardCharsets.UTF_8);
            final JsonElement json = new Gson().fromJson(contents, JsonElement.class);

            // Check for keybinds array
            if(json == null || json.getAsJsonObject() == null || json.getAsJsonObject().get("keybinds") == null) {
                return false;
            }
            final JsonElement keybindsJson = json.getAsJsonObject().get("keybinds");
            if(!keybindsJson.isJsonArray() || keybindsJson.getAsJsonArray() == null) {
                return false;
            }

            // Loop through each keybind
            final JsonArray keybindsArray = keybindsJson.getAsJsonArray();
            for(int i = 0; i < keybindsArray.size(); i++) {
                // Check if the array contains an object at this ith element.
                if(keybindsArray.get(i) == null || keybindsArray.get(i).getAsJsonObject() == null) {
                    continue;
                }
                JsonObject obj = keybindsArray.get(i).getAsJsonObject();
                // Check for the name property on the keybind (which button it targets). If present, assume old version.
                if(obj.get("name") != null && obj.get("name").getAsString() != null) {
                    return true;
                }

            }
            return false;
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * The list of keybinds
     */
    public List<QuickplayKeybind> keybinds = new ArrayList<>();
}
