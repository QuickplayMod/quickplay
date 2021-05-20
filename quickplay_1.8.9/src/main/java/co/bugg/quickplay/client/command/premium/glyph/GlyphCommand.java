package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.client.command.ACommand;

import java.util.List;

public abstract class GlyphCommand extends ACommand {

    public GlyphCommand(ACommand parent, List<String> aliases, String helpMessage, String usage,
                        boolean displayInHelpMenu, boolean displayInTabList, double priority, boolean requiresPremium, int depth) {
        super(parent, aliases, helpMessage, usage, displayInHelpMenu, displayInTabList, priority, requiresPremium, depth);
    }
}
