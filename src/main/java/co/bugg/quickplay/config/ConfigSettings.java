package co.bugg.quickplay.config;

public class ConfigSettings extends AConfiguration {
    public ConfigSettings() {
        super("settings.json");
    }

    public double instanceDisplayX = 0.5;
    public double instanceDisplayY = 0.05;
}
