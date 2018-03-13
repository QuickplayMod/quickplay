package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;

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

                final ITextComponent instanceList = new TextComponentString("");

                for(int i = 0; i < instanceCount; i++) {
                    instanceList.appendText(Quickplay.INSTANCE.instanceWatcher.instanceHistory.get(i) + "\n");
                }

                instanceList.setStyle(new Style().setColor(TextFormatting.YELLOW));

                Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.commands.quickplay.history.header", instanceCount).setStyle(new Style().setColor(TextFormatting.GOLD))));
                Quickplay.INSTANCE.messageBuffer.push(new Message(instanceList));
            } else {
                // Something went wrong
                Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.commands.quickplay.history.error").setStyle(new Style().setColor(TextFormatting.RED))));
            }
        } else {
            // Not online hypixel
            Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.offline").setStyle(new Style().setColor(TextFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
