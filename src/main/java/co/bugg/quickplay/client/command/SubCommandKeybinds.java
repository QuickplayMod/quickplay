package co.bugg.quickplay.client.command;

import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to open the config GUI
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
                new ChatComponentTranslation("quickplay.commands.quickplay.keybinds.help").getUnformattedText(),
                "",
                true,
                true,
                90
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
