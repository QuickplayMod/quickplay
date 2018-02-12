package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import net.minecraft.client.Minecraft;

import java.io.Serializable;
import java.util.HashMap;

public class ConfigSettings extends AConfiguration implements Serializable {
    // TODO make name & helpText into translatable components
    // TODO make runnables into methods
    public ConfigSettings() {
        super("settings.json");
    }

    public double instanceDisplayX = 0.5;
    public double instanceDisplayY = 0.05;

    @GuiOption(
            name = "Primary Color",
            helpText = "Change your Quickplay primary color",
            category = "Colors"
    )
    public QuickplayColor primaryColor = new QuickplayColor(1.0f, 1.0f, 1.0f);
    @GuiOption(
            name = "Secondary Color",
            helpText = "Change your Quickplay secondary color",
            category = "Colors"
    )
    public QuickplayColor secondaryColor = new QuickplayColor(0.7f, 0.7f, 0.7f);

    @GuiOption(
            name = "Blur GUI Backgrounds",
            helpText = "Whether the background of Quickplay GUIs should be slightly blurred.",
            category = "GUI"
    )
    public boolean blurGuiBackgrounds = true;

    @GuiOption(
            name = "Fade GUIs In",
            helpText = "Whether GUIs should fade in when opening.",
            category = "GUI"
    )
    public boolean fadeInGuis = true;

    @GuiOption(
            name = "Instance Display",
            helpText = "Whether or not the current server instance should be displayed on-screen.",
            category = "Instance Display"
    )
    public boolean displayInstance = true;

    @GuiOption(
            name = "Move Instance Display",
            helpText = "Change the position of the Instance display on-screen.",
            category = "Instance Display"
    )
    // TODO Instead of runnable maybe make this a method?
    public transient Runnable moveInstanceDisplayButton = () -> Quickplay.INSTANCE.instanceDisplay.edit();

    @GuiOption(
            name = "Instance Display Opacity",
            helpText = "Opacity of the instance display.",
            category = "Instance Display",
            minValue = 0.0f,
            maxValue = 1.0f
    )
    public double instanceOpacity = 1.0;

    @GuiOption(
            name = "Display Over Chat",
            helpText = "Whether the instance display should be rendered even when chat is open.",
            category = "Instance Display"
    )
    public boolean displayInstanceWithChatOpen = false;

    @GuiOption(
            name = "Game Logo Scale",
            helpText = "Scale of each Hypixel game in main menu",
            category = "GUI",
            minValue = 0.05f,
            maxValue = 2.5f
    )
    public double gameLogoScale = 1.0;

    // Hashmap of custom game priorities for the main game selection GUI
    public HashMap<String, Integer> gamePriorities = new HashMap<>();

    @GuiOption(
            name = "Send Usage Statistics",
            helpText = "Send anonymous usage statistics to help me create better mods for you and diagnose issues with your game/mod if any arise."
    )
    public boolean sendUsageStats = false;

    @GuiOption(
            name = "Edit Keybinds",
            helpText = "Assign & remove keybinds previously created by right-clicking games or modes."
    )
    public transient Runnable editKeybinds = () -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());

    @GuiOption(
            name="Swap to Lobby One",
            helpText = "Swap to lobby one automatically whenever join join a new lobby."
    )
    public boolean lobbyOneSwap = false;
}
