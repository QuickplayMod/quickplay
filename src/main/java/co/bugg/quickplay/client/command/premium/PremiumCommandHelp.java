package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.command.BasicHelpCommand;

/**
 * Command for getting help with Quickplay Premium commands and displaying help text for each Premium command
 */
public class PremiumCommandHelp extends BasicHelpCommand {

    public PremiumCommandHelp(ACommand parent) {
        super(
                parent,
                "Displays Premium command help information",
                "",
                true,
                true,
                91,
                false
        );
    }
}
