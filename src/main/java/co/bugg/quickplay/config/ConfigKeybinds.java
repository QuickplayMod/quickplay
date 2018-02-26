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
}
