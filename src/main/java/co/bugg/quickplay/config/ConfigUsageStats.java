package co.bugg.quickplay.config;

import java.io.Serializable;
import java.util.UUID;

public class ConfigUsageStats extends AConfiguration implements Serializable {

    /**
     * Constructor
     */
    public ConfigUsageStats() {
        super("privacy.json");
    }

    @GuiOption(
            name = "Send Usage Statistics",
            helpText = "Send anonymous usage statistics to help me create better mods for you!"
    )
    public boolean sendUsageStats = false;

    public UUID statsToken = UUID.randomUUID();
}
