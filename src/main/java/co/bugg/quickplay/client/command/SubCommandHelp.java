package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;

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
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.help.help"),
                true,
                true,
                100.0,
                false
        );
    }
}
