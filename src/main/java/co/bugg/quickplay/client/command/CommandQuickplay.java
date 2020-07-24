package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.premium.SubCommandPremium;
import co.bugg.quickplay.util.InvalidCommandException;
import net.minecraft.command.ICommandSender;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * /quickplay command
 */
@ParametersAreNonnullByDefault
public class CommandQuickplay extends BaseCommand {

    final SubCommandHelp helpCommand;

    /**
     * Constructor
     */
    public CommandQuickplay() {
        super("/quickplay", "quickplay", "qp");
        this.helpCommand = new SubCommandHelp(this);

        addSubCommand(this.helpCommand);
        addSubCommand(new SubCommandLimbo(this));
        addSubCommand(new SubCommandDelivery(this));
        addSubCommand(new SubCommandKeybinds(this));
        addSubCommand(new SubCommandConfig(this));
        addSubCommand(new SubCommandPremium(this));
        addSubCommand(new SubCommandHistory(this));
        addSubCommand(new SubCommandDiscord(this));
        addSubCommand(new SubCommandParty(this));
        addSubCommand(new SubCommandRefreshResource(this));
        addSubCommand(new SubCommandWiki(this));
        addSubCommand(new SubCommandVersion(this));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(Quickplay.INSTANCE.checkEnabledStatus()) {
            super.processCommand(sender, args);
        }
    }

    @Override
    public void run(String[] args) {
        if(args.length == 0) {
            this.helpCommand.run(new String[0]);
            return;
        }
        try {
            super.run(args);
        } catch(InvalidCommandException e) {
            this.helpCommand.run(new String[0]);
        }
    }
}
