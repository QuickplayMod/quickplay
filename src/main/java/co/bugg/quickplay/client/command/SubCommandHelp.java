package co.bugg.quickplay.client.command;

import net.minecraft.client.resources.I18n;

/**
 * Sub command for the "help" message
 */
public class SubCommandHelp extends BasicHelpCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandHelp(ACommand parent) {
        super(
                parent,
                I18n.format("quickplay.commands.quickplay.help.help"),
                true,
                true,
                100.0,
                false
        );
    }
}
