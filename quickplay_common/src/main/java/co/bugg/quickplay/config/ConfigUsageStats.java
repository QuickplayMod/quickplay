package co.bugg.quickplay.config;

import java.io.Serializable;
import java.util.UUID;

/**
 * Configuration for changing the user's privacy settings
 */
public class ConfigUsageStats extends AConfiguration implements Serializable {

    /**
     * Constructor
     */
    public ConfigUsageStats() {
        super("privacy.json");
    }

    /**
     * Whether the user wants to send usage statistics to Quickplay backend
     */
    @GuiOption(
            name = "Send Usage Statistics",
            helpText = "Send anonymous usage statistics to help me create better mods for you!"
    )
    public boolean sendUsageStats = false;

    /**
     * This user's unique statistics token
     */
    public UUID statsToken = UUID.randomUUID();
}
