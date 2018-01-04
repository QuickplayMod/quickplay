package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import net.minecraft.command.ICommandSender;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * /quickplay command
 */
@ParametersAreNonnullByDefault
public class CommandQuickplay extends ASubCommandParent {

    /**
     * Constructor
     */
    public CommandQuickplay() {
        super("quickplay", "qp");

        addSubCommand(new SubCommandHelp(this));
        addSubCommand(new SubCommandLimbo(this));
        addSubCommand(new SubCommandMoveInstance(this));
        addSubCommand(new SubCommandConfig(this));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(Quickplay.INSTANCE.checkEnabledStatus()) {
            super.processCommand(sender, args);
        }
    }
}
