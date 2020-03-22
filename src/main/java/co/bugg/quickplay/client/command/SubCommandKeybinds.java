package co.bugg.quickplay.client.command;

import cc.hyperium.Hyperium;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.I18n;

/**
 * Sub command to open the keybinds GUI
 */
public class SubCommandKeybinds extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandKeybinds(ACommand parent) {
        super(
                parent,
                "keybinds",
                I18n.format("quickplay.commands.quickplay.keybinds.help"),
                "",
                true,
                true,
                80
        );
    }

    @Override
    public void run(String[] args) {
        Hyperium.INSTANCE.getHandlers().getGuiDisplayHandler().setDisplayNextTick(new QuickplayGuiKeybinds());
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
