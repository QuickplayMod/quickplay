package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.game.QuickplayGuiMainMenu;
import org.lwjgl.input.Keyboard;

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
     * The list of keybinds
     */
    public List<QuickplayKeybind> keybinds = new ArrayList<>();
}
