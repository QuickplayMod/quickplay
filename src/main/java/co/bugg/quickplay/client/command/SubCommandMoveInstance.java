package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
        Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.commands.quickplay.moveinstance.deprecated").setStyle(new Style().setColor(TextFormatting.RED))));
        new TickDelay(() -> Quickplay.INSTANCE.instanceDisplay.edit(), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
