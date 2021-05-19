package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.InvalidCommandException;

import java.util.Collections;

/**
 * Sub command for Quickplay Premium
 */
public class SubCommandPremium extends ACommand {

    final PremiumCommandHelp helpCommand;

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandPremium(ACommand parent) {
        super(
                parent,
                Collections.singletonList("premium"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.help"),
                "help",
                true,
                true,
                91,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );

        this.helpCommand = new PremiumCommandHelp(this);
        this.addSubCommand(this.helpCommand);
        this.addSubCommand(new PremiumCommandAbout(this));
        this.addSubCommand(new PremiumCommandAccount(this));
        this.addSubCommand(new PremiumCommandTransfer(this));
        this.addSubCommand(new PremiumCommandGlyph(this));
        this.addSubCommand(new PremiumCommandAuth(this));
    }

    @Override
    public void run(String[] args) {
        if(args.length == this.getDepth()) {
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
