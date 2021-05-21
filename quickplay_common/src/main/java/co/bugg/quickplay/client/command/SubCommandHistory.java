package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatComponentTextWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import co.bugg.quickplay.wrappers.chat.IChatComponentWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to display instance history
 */
public class SubCommandHistory extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandHistory(ACommand parent) {
        super(
                parent,
                Collections.singletonList("history"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.history.help"),
                "[count]",
                true,
                true,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.isOnHypixel()) {
            if(Quickplay.INSTANCE.hypixelInstanceWatcher != null && Quickplay.INSTANCE.hypixelInstanceWatcher.instanceHistory != null) {

                // Get how many isntances to display
                // Default # of instances to display
                final int defaultInstanceCount = 20;
                int instanceCount;
                if (args.length > 0) {
                    try {
                        instanceCount = Math.abs(Integer.parseInt(args[0]));
                    } catch(NumberFormatException e) {
                        instanceCount = defaultInstanceCount;
                    }
                } else {
                    instanceCount = defaultInstanceCount;
                }

                // If the number provided by the user is too large, use the max
                instanceCount = Math.min(Quickplay.INSTANCE.hypixelInstanceWatcher.instanceHistory.size(), instanceCount);

                final IChatComponentWrapper instanceList = new ChatComponentTextWrapper("");

                for(int i = 0; i < instanceCount; i++) {
                    instanceList.appendText(Quickplay.INSTANCE.hypixelInstanceWatcher.instanceHistory.get(i) + "\n");
                }

                instanceList.setStyle(new ChatStyleWrapper().apply(Formatting.YELLOW));

                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.commands.quickplay.history.header", String.valueOf(instanceCount))
                        .setStyle(new ChatStyleWrapper().apply(Formatting.GOLD))));
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(instanceList));
            } else {
                // Something went wrong
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.commands.quickplay.history.error")
                        .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
            }
        } else {
            // Not online hypixel
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                    "quickplay.offline")
                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
