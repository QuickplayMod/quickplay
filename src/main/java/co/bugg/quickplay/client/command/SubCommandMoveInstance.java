package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to move the current instance display around
 */
@Deprecated // Use the main configuration instead
public class SubCommandMoveInstance extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandMoveInstance(ACommand parent) {
        super(
                parent,
                "moveinstance",
                "Move the Instance display around",
                "",
                false,
                false,
                -100.0
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.moveinstance.deprecated").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        new TickDelay(() -> Quickplay.INSTANCE.instanceDisplay.edit(), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
