package co.bugg.quickplay.client.command;

import cc.hyperium.Hyperium;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command for the limbo command
 */
public class SubCommandLimbo extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandLimbo(ACommand parent) {
        super(
                parent,
                "limbo",
                I18n.format("quickplay.commands.quickplay.limbo.help"),
                "",
                true,
                true,
                -90.0
        );
    }

    @Override
    public void run(String[] args) {
        if(Hyperium.INSTANCE.getHandlers().getHypixelDetector().isHypixel()) {
            String currentServer = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
            if(currentServer == null) currentServer = "null";

            // TODO false positive in Megawalls lobby
            if(currentServer.contains("mini") || currentServer.contains("mega")) {
                Quickplay.INSTANCE.chatBuffer.push("/achat §");

                // Sleep for a sec to give time to get to the lobby
                // Just try regardless to go to limbo if interrupted
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Hyperium.LOGGER.error(e.getMessage(), e);
                    Quickplay.INSTANCE.chatBuffer.push("/achat §");
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                    return;
                }
            }

            if(!currentServer.equals("limbo"))
                Quickplay.INSTANCE.chatBuffer.push("/achat §");
            else
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.limbo.alreadythere").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.offline").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
