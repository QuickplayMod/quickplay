package co.bugg.quickplay.client.command;

import cc.hyperium.Hyperium;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.config.QuickplayGuiEditConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.I18n;

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
                I18n.format("quickplay.commands.quickplay.config.help"),
                "",
                true,
                true,
                99.9
        );
    }

    @Override
    public void run(String[] args) {
        Hyperium.INSTANCE.getHandlers().getGuiDisplayHandler().setDisplayNextTick(new QuickplayGuiEditConfig(Quickplay.INSTANCE.settings));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
