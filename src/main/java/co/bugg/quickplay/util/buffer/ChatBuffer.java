package co.bugg.quickplay.util.buffer;


import cc.hyperium.Hyperium;
import cc.hyperium.event.InvokeEvent;
import cc.hyperium.event.ServerLeaveEvent;
import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Buffer for chat messages sent AS the client.
 * Will send all messages as the client as soon
 * as possible. Buffer is cleared when the player
 * disconnects from a server.
 */
public class ChatBuffer extends ABuffer {

    /**
     * Constructor
     *
     * @param sleepTime Time in milliseconds between {@link #run()} calls. See {@link #sleepTime}
     */
    public ChatBuffer(int sleepTime) {
        super(sleepTime);
    }

    /**
     * Start sending messages in the
     * buffer, whenever possible
     */
    public void run() {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        // Only send a message if the player exists & there is a message to send
        if(size() > 0 && player != null) {
            final String message = (String) pull();

            // Handle as a command
            if(message.startsWith("/") && !Hyperium.INSTANCE.getHandlers().getHyperiumCommandHandler().executeCommand(message))
                player.sendChatMessage(message);
        }
    }

    @Override
    public ABuffer start() {
        Quickplay.INSTANCE.registerEventHandler(this);
        return super.start();
    }

    @Override
    public ABuffer stop() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
        return super.stop();
    }

    @InvokeEvent
    public void onDisconnect(ServerLeaveEvent event) {
        clear();
    }


}
