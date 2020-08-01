package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.config.ConfigSettings;

import java.io.IOException;

public class ResetConfigAction extends Action {
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
