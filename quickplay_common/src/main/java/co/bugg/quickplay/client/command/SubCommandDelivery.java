package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command for the delivery menu
 */
public class SubCommandDelivery extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDelivery(ACommand parent) {
        super(
                parent,
                Collections.singletonList("delivery"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.delivery.help"),
                "",
                true,
                true,
                -100.0,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.isOnHypixel()) {
            String currentServer = Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentServer();
            if(currentServer == null) currentServer = "null";

            if((currentServer.contains("mini") || currentServer.contains("mega") || currentServer.contains("limbo")) &&
                    !currentServer.contains("walls")) {
                if(currentServer.contains("limbo")) {
                    Quickplay.INSTANCE.minecraft.sendRemoteMessage("/lobby");
                } else {
                    Quickplay.INSTANCE.minecraft.sendRemoteMessage("/achat §");
                }

                // Sleep for a sec to give time to get to the lobby
                // Just try regardless to open if interrupted
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.minecraft.sendRemoteMessage("/delivery");
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }

            Quickplay.INSTANCE.minecraft.sendRemoteMessage("/delivery");
        } else {
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.offline")
                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
