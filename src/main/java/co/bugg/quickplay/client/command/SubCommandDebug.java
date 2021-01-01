package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to enable debug mode within the client - Will output action data to the log as it's received.
 */
public class SubCommandDebug extends ACommand {

    // TODO remove
    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDebug(ACommand parent) {
        super(
                parent,
                Collections.singletonList("debug"),
                 "",
                "",
                false,
                false,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.isInDebugMode = !Quickplay.INSTANCE.isInDebugMode;
        Quickplay.INSTANCE.messageBuffer.push(new Message(
                new ChatComponentText(Quickplay.INSTANCE.isInDebugMode ? "DEBUG ON":"DEBUG OFF").setChatStyle(
                        new ChatStyle().setColor(Quickplay.INSTANCE.isInDebugMode ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
                )));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
