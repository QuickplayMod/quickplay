package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.util.TickDelay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to open the keybinds GUI
 */
public class SubCommandKeybinds extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandKeybinds(ACommand parent) {
        super(
                parent,
                Collections.singletonList("keybinds"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.keybinds.help"),
                "",
                true,
                true,
                80,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        new TickDelay(() -> Quickplay.INSTANCE.minecraft.openGui(new QuickplayGuiKeybinds()), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
