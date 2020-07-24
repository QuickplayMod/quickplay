package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.command.BasicHelpCommand;

/**
 * Command for getting help with Quickplay Premium Glyph commands and displaying help text for each command
 */
public class GlyphCommandHelp extends BasicHelpCommand {

    public GlyphCommandHelp(ACommand parent) {
        super(
                parent,
                "Displays Glyph command help information",
                "",
                true,
                true,
                80,
                false
        );
    }
}
