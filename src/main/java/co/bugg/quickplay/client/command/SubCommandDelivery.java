package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command for the limbo command
 */
public class SubCommandDelivery extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDelivery(ACommand parent) {
        super(
                parent,
                "delivery",
                new ChatComponentTranslation("quickplay.commands.quickplay.delivery.help").getUnformattedText(),
                "",
                true,
                true,
                -100.0
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.onHypixel) {
            String currentServer = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
            if(currentServer == null) currentServer = "null";

            if(currentServer.contains("mini") || currentServer.contains("mega") || currentServer.contains("limbo")) {
                if(currentServer.contains("limbo"))
                    Quickplay.INSTANCE.chatBuffer.push("/lobby");
                else
                    Quickplay.INSTANCE.chatBuffer.push("/achat §");

                // Sleep for a sec to give time to get to the lobby
                // Just try regardless to open if interrupted
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.chatBuffer.push("/delivery");
                    return;
                }
            }

            Quickplay.INSTANCE.chatBuffer.push("/delivery");
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.offline").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
