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
    private ArrayList<Message> buffer;

    public MessageBuffer() {
        buffer = new ArrayList<>();
    }

    public void push(Message message) {
        buffer.add(message);
    }

    public Message peek() {
        return buffer.get(0);
    }

    public Message pull() {
        Message message = peek();
        buffer.remove(0);

        return message;
    }

    public int size() {
        return buffer.size();
    }

    public void run() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

                try {
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
