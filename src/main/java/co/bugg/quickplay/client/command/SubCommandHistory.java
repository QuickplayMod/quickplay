package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to display instance history
 */
public class SubCommandHistory extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandHistory(ACommand parent) {
        super(
                parent,
                "history",
                I18n.format("quickplay.commands.quickplay.history.help"),
                "[count]",
                true,
                true,
                85
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.onHypixel) {
            if(Quickplay.INSTANCE.instanceWatcher != null && Quickplay.INSTANCE.instanceWatcher.instanceHistory != null) {

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
                instanceCount = Math.min(Quickplay.INSTANCE.instanceWatcher.instanceHistory.size(), instanceCount);

                final IChatComponent instanceList = new ChatComponentText("");

                for(int i = 0; i < instanceCount; i++) {
                    instanceList.appendText(Quickplay.INSTANCE.instanceWatcher.instanceHistory.get(i) + "\n");
                }

                instanceList.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));

                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.history.header", instanceCount).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD))));
                Quickplay.INSTANCE.messageBuffer.push(new Message(instanceList));
            } else {
                // Something went wrong
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.history.error").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }
        } else {
            // Not online hypixel
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.offline").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
