package co.bugg.quickplay.util.buffer;


import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Buffer for chat messages sent AS the client.
 * Will send all messages as the client as soon
 * as possible. Buffer is cleared when the player
 * disconnects from a server.
 */
public class ChatBuffer extends ABuffer<String> {

    /**
     * Constructor
     * @param burstDelay Delay between buffer pulls when in "burst" mode.
     * @param burstCap Total number of items allowed per burst.
     * @param burstCooldown Total time between bursts, before another burst can occur.
     * @param normalDelay Delay between buffer pulls when not in "burst" mode.
     */
    public ChatBuffer(int burstDelay, int burstCap, int burstCooldown, int normalDelay) {
        super(burstDelay, burstCap, burstCooldown, normalDelay);
    }

    /**
     * Start sending messages in the
     * buffer, whenever possible
     */
    public void run() {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        // Only send a message if the player exists & there is a message to send
        if(size() > 0 && player != null) {
            String message = this.pull();

            // Handle as a command
            if(!message.startsWith("/")) {
                message = "/" + message;
            }
            if(ClientCommandHandler.instance.executeCommand(player, message) == 0) {
                player.sendChatMessage(message);
            }
        }
    }

    @Override
    public ABuffer<String> start() {
        Quickplay.INSTANCE.registerEventHandler(this);
        return super.start();
    }

    @Override
    public ABuffer<String> stop() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
        return super.stop();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.clear();
    }


}
