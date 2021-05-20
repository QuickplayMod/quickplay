package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.command.premium.glyph.*;
import co.bugg.quickplay.util.InvalidCommandException;

import java.util.Arrays;
import java.util.ListIterator;

public class PremiumCommandGlyph extends ACommand {

    GlyphCommandHelp helpCommand;

    public PremiumCommandGlyph(ACommand parent) {
        super(
                parent,
                Arrays.asList("glyph", "icon"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.glyph.help"),
                "",
                true,
                true,
                99.9,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
        this.helpCommand = new GlyphCommandHelp(this);
        this.addSubCommand(this.helpCommand);
        this.addSubCommand(new GlyphCommandSet(this));
        this.addSubCommand(new GlyphCommandOffset(this));
        this.addSubCommand(new GlyphCommandHeight(this));
        this.addSubCommand(new GlyphCommandReset(this));
        this.addSubCommand(new GlyphCommandDisplayInGames(this));
    }

    @Override
    public String getUsage() {
        final StringBuilder builder = new StringBuilder("[");
        for(ListIterator<ACommand> iter = this.subCommands.listIterator(); iter.hasNext();) {
            final ACommand cmd = iter.next();

            builder.append(cmd.getName());
            // If this command takes any parameters
            if(cmd.getUsage() != null && cmd.getUsage().length() > 0) {
                builder.append(" ");
                builder.append(cmd.getUsage());
            }
            // If there's another command
            if(iter.hasNext())
                builder.append("|");
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public void run(String[] args) {
        if (args.length == this.getDepth()) {
            this.helpCommand.run(new String[0]);
            return;
        }
        try {
            super.run(args);
        } catch (InvalidCommandException e) {
            this.helpCommand.run(new String[0]);
        }
    }
}
