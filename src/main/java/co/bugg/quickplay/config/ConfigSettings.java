package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.TickDelay;

import java.awt.*;

public class ConfigSettings extends AConfiguration {
    // TODO add priority
    // TODO Add hover help text to EditConfiguration
    // TODO make name & helpText into translatable components
    public ConfigSettings() {
        super("settings.json");
    }

    public double instanceDisplayX = 0.5;
    public double instanceDisplayY = 0.05;

    @GuiOption(
            name = "Primary Color",
            helpText = "Change your Quickplay primary color"
    )
    public Color primaryColor = new Color(1.0f, 1.0f, 1.0f);
    @GuiOption(
            name = "Secondary Color",
            helpText = "Change your Quickplay secondary color"
    )
    public Color secondaryColor = new Color(0.7f, 0.7f, 0.7f);

    @GuiOption(
            name = "Instance Display",
            helpText = "Whether or not the current server instance should be displayed on-screen."
    )
    public boolean displayInstance = true;

    @GuiOption(
            name = "Move Instance Display",
            helpText = "Change the position of the Instance display on-screen."
    )
    // TODO Instead of runnable maybe make this a method?
    public transient Runnable moveInstanceDisplayButton = () -> Quickplay.INSTANCE.instanceDisplay.edit();

    @GuiOption(
            name = "Instance Display Opacity",
            helpText = "Opacity of the instance display.",
            minValue = 0.0f,
            maxValue = 1.0f
    )
    public double instanceOpacity = 1.0;

    @GuiOption(
            name = "Fade GUIs In",
            helpText = "Whether GUIs should fade in when opening."
    )
    public boolean fadeInGuis = true;
}
