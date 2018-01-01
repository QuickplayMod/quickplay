package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.TickDelay;

public class ConfigSettings extends AConfiguration {
    public ConfigSettings() {
        super("settings.json");
    }

    public double instanceDisplayX = 0.5;
    public double instanceDisplayY = 0.05;

    @GuiOption(
            name = "Move Instance Display",
            helpText = "Change the position of the Instance display on-screen"
    )
    // TODO Instead of runnable maybe make this a method?
    public transient Runnable moveInstanceDisplayButton = () -> {
        new TickDelay(() -> Quickplay.INSTANCE.instanceDisplay.edit(), 1);
    };
}
