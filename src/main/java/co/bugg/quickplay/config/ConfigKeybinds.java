package co.bugg.quickplay.config;

import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.game.QuickplayGuiMainMenu;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ConfigKeybinds extends AConfiguration {

    public ConfigKeybinds() {
        super("keybinds.json");
    }

    public ConfigKeybinds(boolean addDefaultKeybinds) {
        this();
        if(addDefaultKeybinds) {
            keybinds.add(new QuickplayKeybind(new ChatComponentTranslation("quickplay.config.keybinds.openmain").getUnformattedText(), Keyboard.KEY_R, QuickplayGuiMainMenu.class));
        }
    }

    public List<QuickplayKeybind> keybinds = new ArrayList<>();

    // TODO on startup, verify the user's keybinds to get rid of any spam. Any exact duplicates (both the same bound action AND the same key) can be removed.
    // TODO Also make it so keybinds can't be duplicated maybe to avoid server spam (like every /play command bound to one key oml)
}
