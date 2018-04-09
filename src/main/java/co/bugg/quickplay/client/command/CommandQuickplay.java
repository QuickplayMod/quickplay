package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.premium.SubCommandPremium;
import net.minecraft.command.ICommandSender;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * /quickplay command
 */
@ParametersAreNonnullByDefault
public class CommandQuickplay extends ACommand {

    /**
     * Constructor
     */
    public CommandQuickplay() {
        super("quickplay", "qp");

        addSubCommand(new SubCommandHelp(this));
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
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(Quickplay.INSTANCE.checkEnabledStatus()) {
            super.processCommand(sender, args);
        }
    }
}
