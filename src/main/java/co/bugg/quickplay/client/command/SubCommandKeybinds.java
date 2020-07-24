package co.bugg.quickplay.client.command;

import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

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
                I18n.format("quickplay.commands.quickplay.keybinds.help"),
                "",
                true,
                true,
                80
        );
    }

    @Override
    public void run(String[] args) {
        new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds()), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
