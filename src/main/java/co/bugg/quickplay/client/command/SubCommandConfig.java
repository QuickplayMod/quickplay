package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.config.EditConfiguration;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to open the config GUI
 */
public class SubCommandConfig extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandConfig(ACommand parent) {
        super(
                parent,
                "config",
                new ChatComponentTranslation("quickplay.commands.quickplay.config.help").getUnformattedText(),
                "",
                true,
                true,
                99.9
        );
    }

    @Override
    public void run(String[] args) {
        new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new EditConfiguration(Quickplay.INSTANCE.settings)), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
