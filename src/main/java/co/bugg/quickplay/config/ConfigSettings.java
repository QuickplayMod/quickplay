package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;

import java.awt.*;

public class ConfigSettings extends AConfiguration {
    // TODO Add hover help text to EditConfiguration
    // TODO make name & helpText into translatable components
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
            helpText = "Whether the instance display should be rendered even when chat is open",
            category = "Instance Display"
    )
    public boolean displayInstanceWithChatOpen = false;
}
