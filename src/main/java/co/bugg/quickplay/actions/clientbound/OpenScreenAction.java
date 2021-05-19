package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.Screen;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.gui.QuickplayGuiScreen;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.nio.ByteBuffer;

/**
 * ID: 11
 * Open a screen on the client. The client should have already been sent this screen this session.
 *
 * Payload Order:
 * screen name
 */
public class OpenScreenAction extends Action {

    public OpenScreenAction() {}

    /**
     * Create a new OpenScreenAction.
     * @param screenName The name of the screen that the client should open. Should not be null.
     */
    public OpenScreenAction(String screenName) {
        super();
        this.id = 11;
        this.addPayload(ByteBuffer.wrap(screenName.getBytes()));
    }

    @Override
    public void run() {
        final Screen screen = Quickplay.INSTANCE.elementController.getScreen(this.getPayloadObjectAsString(0));
        if(screen == null || !screen.passesPermissionChecks()) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.screenOpenFail")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
                    , false, false));
            return;
        }


        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiScreen(screen));
        });
    }
}
