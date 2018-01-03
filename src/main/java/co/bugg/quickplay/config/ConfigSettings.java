package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.TickDelay;

import java.awt.*;

public class ConfigSettings extends AConfiguration {
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
            name = "Move Instance Display",
            helpText = "Change the position of the Instance display on-screen"
    )
    // TODO Instead of runnable maybe make this a method?
    public transient Runnable moveInstanceDisplayButton = () -> {
        new TickDelay(() -> Quickplay.INSTANCE.instanceDisplay.edit(), 1);
    };
}
