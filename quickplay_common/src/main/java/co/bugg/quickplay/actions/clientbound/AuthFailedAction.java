package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

/**
 * ID: 39
 * Notify the client that authentication failed, doesn't specify the reason.
 */
public class AuthFailedAction extends Action {

    /**
     * Create a new AuthFailedAction.
     */
    public AuthFailedAction() {
        super();
        this.id = 39;
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                "quickplay.failedToAuth"
        ).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
    }
}
