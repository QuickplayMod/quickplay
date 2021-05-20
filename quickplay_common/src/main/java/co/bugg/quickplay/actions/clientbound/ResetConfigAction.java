package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.config.ConfigSettings;

import java.io.IOException;

/**
 * ID: 5
 * Reset the client's configuration. Use sparingly.
 */
public class ResetConfigAction extends Action {

    /**
     * Create a new ResetConfigAction.
     */
    public ResetConfigAction() {
        super();
        this.id = 5;
    }

    @Override
    public void run() {
        // Overwrite settings
        Quickplay.INSTANCE.settings = new ConfigSettings();
        Quickplay.INSTANCE.keybinds = new ConfigKeybinds(true);
        // Overwrite file containing cached settings
        try {
            Quickplay.INSTANCE.settings.save();
            Quickplay.INSTANCE.keybinds.save();
        } catch (IOException e) {
            System.out.println("Failed to save file while overwriting settings");
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
