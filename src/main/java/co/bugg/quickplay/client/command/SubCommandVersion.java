package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub command to open up the Quickplay wiki
 */
public class SubCommandVersion extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandVersion(ACommand parent) {
        super(
                parent,
                Arrays.asList("version", "v"),
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.version.help"),
                "",
                false,
                true,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                "quickplay.commands.quickplay.version.message", Reference.VERSION)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD))));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
