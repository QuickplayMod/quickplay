package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Sub command for the limbo command
 */
public class SubCommandLimbo extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandLimbo(ASubCommandParent parent) {
        super(
                parent,
                "limbo",
                new ChatComponentTranslation("quickplay.commands.quickplay.limbo.help").getUnformattedText(),
                "",
                true,
                true,
                90.0
        );
    }

    @Override
    public void run(String[] args) {
        // TODO: Get current server & go to hub first if necessary
        if(Quickplay.INSTANCE.onHypixel) {
            Quickplay.INSTANCE.chatBuffer.push("/achat §");
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new ChatComponentTranslation("quickplay.commands.quickplay.limbo.offline").getChatStyle().setColor(EnumChatFormatting.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
