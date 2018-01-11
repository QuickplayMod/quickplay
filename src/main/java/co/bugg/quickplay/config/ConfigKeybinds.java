package co.bugg.quickplay.config;

import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.game.QuickplayGuiMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigKeybinds extends AConfiguration {

    public ConfigKeybinds() {
        super("keybinds.json");
    }


    private transient List<QuickplayKeybind> defaultKeybinds = new ArrayList<>(Arrays.asList(
            new QuickplayKeybind(new ChatComponentTranslation("quickplay.config.keybinds.openmain").getUnformattedText(), Keyboard.KEY_R) {
                @Override
                public void keyPressed() {
                    Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiMainMenu());
                    System.out.println("mainmenu open");
                }
            }
    ));

    public List<QuickplayKeybind> keybinds = new ArrayList<>(defaultKeybinds);
}
