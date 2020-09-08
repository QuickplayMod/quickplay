package co.bugg.quickplay.util.buffer;


import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 */
public class MessageBuffer extends ABuffer {

    /**
     * Constructor
     *
     * @param normalDelay Time in milliseconds between {@link #run()} calls.
     */
    public MessageBuffer(int normalDelay) {
        super(normalDelay);
    }

    /**
     * Start sending messages in the
     * buffer, whenever possible
     */
    public void run() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        // Only send a message if the player exists & there is a message to send
        if(size() > 0 && player != null) {
            player.addChatMessage(((Message) pull()).getMessage());
        }
    }


}
