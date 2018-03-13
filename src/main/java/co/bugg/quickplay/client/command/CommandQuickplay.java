package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.premium.SubCommandPremium;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
        addSubCommand(new SubCommandMoveInstance(this));
        addSubCommand(new SubCommandConfig(this));
        addSubCommand(new SubCommandPremium(this));
        addSubCommand(new SubCommandHistory(this));
        addSubCommand(new SubCommandDiscord(this));
        addSubCommand(new SubCommandParty(this));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(Quickplay.INSTANCE.checkEnabledStatus()) {
            super.execute(sender, args);
        }
    }
}
