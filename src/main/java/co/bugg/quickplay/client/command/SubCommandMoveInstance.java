package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.MoveableHudElementEditor;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to move the current instance display around
 * TODO this should be removed later and put into the config GUI, but is implemented for testing
 */
public class SubCommandMoveInstance extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandMoveInstance(ASubCommandParent parent) {
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
        new TickDelay(() -> Quickplay.INSTANCE.instanceDisplay.edit(), 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
