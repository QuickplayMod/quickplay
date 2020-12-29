package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command for the limbo command
 */
public class SubCommandLimbo extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandLimbo(ACommand parent) {
        super(
                parent,
                Collections.singletonList("limbo"),
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.limbo.help"),
                "",
                true,
                true,
                -90.0,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.onHypixel) {
            String currentServer = Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentServer();
            if(currentServer == null) currentServer = "null";

            if(currentServer.contains("mini") || currentServer.contains("mega") && !currentServer.contains("walls")) {
                Quickplay.INSTANCE.chatBuffer.push("/achat §");

                // Sleep for a sec to give time to get to the lobby
                // Just try regardless to go to limbo if interrupted
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.chatBuffer.push("/achat §");
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                    return;
                }
            }

            if(!currentServer.equals("limbo")) {
                Quickplay.INSTANCE.chatBuffer.push("/achat §");
            } else {
                Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.commands.quickplay.limbo.alreadythere")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                    "quickplay.offline").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
