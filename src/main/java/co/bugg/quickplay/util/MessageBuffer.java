package co.bugg.quickplay.util;


import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.*;

import java.util.ArrayList;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 */
public class MessageBuffer {
    /**
     * Buffer of messages to send
     */
    private ArrayList<Message> buffer;

    /**
     * Constructor
     */
    public MessageBuffer() {
        buffer = new ArrayList<>();
    }

    /**
     * Push a new message to the buffer
     * @param message Message to push
     */
    public void push(Message message) {
        buffer.add(message);
    }

    /**
     * Peek at the next message in the buffer
     * @return Next message in the buffer
     */
    public Message peek() {
        return buffer.get(0);
    }

    /**
     * Pull the next message out of the buffer
     * @return The message being pulled
     */
    public Message pull() {
        Message message = peek();
        buffer.remove(0);

        return message;
    }

    /**
     * Get the size of the buffer
     * @return Buffer size
     */
    public int size() {
        return buffer.size();
    }

    /**
     * Start sending messages in the
     * buffer, whenever possible
     */
    public void run() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

                try {
                    // Only send a message if the player exists & there is a message to send
                    if(size() > 0 && player != null) {
                        player.addChatMessage(pull().getMessage());
                    }

                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }


}
