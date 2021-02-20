package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
        if(Quickplay.INSTANCE.onHypixel) {
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
                    e.printStackTrace();
                    Quickplay.INSTANCE.chatBuffer.push("/achat §");
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                    return;
                }
            }

            if(!currentServer.equals("limbo")) {
                Quickplay.INSTANCE.chatBuffer.push("/achat §");
            } else {
                Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation(
                        "quickplay.commands.quickplay.limbo.alreadythere")
                        .setStyle(new Style().setColor(TextFormatting.RED))));
            }
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation(
                    "quickplay.offline").setStyle(new Style().setColor(TextFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
