package co.bugg.quickplay.util.buffer;


import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 */
public class MessageBuffer extends ABuffer<Message> {

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

        // Only send a message if the player exists & there is a message to send
        if(size() > 0) {
            final Message obj = this.pull();
            if(obj == null) {
                return;
            }
            Quickplay.INSTANCE.minecraft.sendLocalMessage(obj);
        }
    }


}
