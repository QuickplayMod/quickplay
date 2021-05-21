package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 6
 * Send a command to the server the client is connected to.
 */
public class SendChatCommandAction extends Action {

    public SendChatCommandAction() {}

    /**
     * Create a new SendChatCommandAction.
     * @param cmd Command to send. Beginning slash will automatically be removed if provided,
     * and the client will add it back. To run a command that begins with two slashes (e.g. //wand, like WorldEdit), you
     * must provide both slashes. Should be non-null.
     */
    public SendChatCommandAction(String cmd) {
        super();
        this.id = 6;
        if(cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        this.addPayload(ByteBuffer.wrap(cmd.getBytes()));
    }

    @Override
    public void run() {
        String cmd = "/" + this.getPayloadObjectAsString(0);
        Quickplay.INSTANCE.minecraft.sendRemoteMessage(cmd);
    }
}
