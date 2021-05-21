package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

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
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.version.help"),
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
        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                "quickplay.commands.quickplay.version.message", Reference.VERSION)
                .setStyle(new ChatStyleWrapper().apply(Formatting.GOLD))));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
