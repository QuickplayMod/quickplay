package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * When online Hypixel, enabled instances of
 * this class will watch for what instance the
 * client is on by occasionally executing
 * /whereami
 */
public class InstanceWatcher {
    /**
     * List of all instances in this game session
     * Index 0 is the latest
     */
    public List<String> instanceHistory = new ArrayList<>();
    /**
     * Whether the instance is running & registered
     * with the event handler
     */
    public boolean started = false;
    /**
     * How often in seconds /whereami should be executed
     */
    public int whereamiFrequency;
    /**
     * Whether the /whereami messages should be cancelled
     */
    public boolean cancelWhereamiMessages;

    public InstanceWatcher(int frequency) {
        whereamiFrequency = frequency;
    }

    public int tick;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START && tick++ > whereamiFrequency * 20) {
            tick = 0;
            sendWhereami();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();
        // Regex for the /whereami response
        Pattern pattern = Pattern.compile("§bYou are currently (?:(?:in |connected to server §r§6)(limbo|(?:(?:[A-Za-z]+)?lobby(?:\\d{1,3})|(?:mega|mini)\\d{1,3}[A-Z])))§r");
        Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {
            if(cancelWhereamiMessages) {
                event.setCanceled(true);
                this.cancelWhereamiMessages = false;
            }

            String server = matcher.group(1);
            if(instanceHistory.size() <= 0 || !instanceHistory.get(0).equals(server)) {
                instanceHistory.add(0, server);
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        // This event is typically called three times, for each dimension
        // Only send the "whereami" message if it's for the overworld
        if(event.world.provider.getDimensionId() == 0) {
            new TickDelay(this::sendWhereami, 30);
        }
    }

    /**
     * Start the event handler tick loop & listen for chat messages
     * @return this
     */
    public InstanceWatcher start() {
        Quickplay.INSTANCE.registerEventHandler(this);
        started = true;
        sendWhereami();
        return this;
    }

    /**
     * Stop the event handler
     * @return this
     */
    public InstanceWatcher stop() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
        started = false;
        return this;
    }

    /**
     * Send the /whereami message if possible
     * @return this
     */
    public InstanceWatcher sendWhereami() {
        if(Quickplay.INSTANCE.onHypixel) {
            cancelWhereamiMessages = true;
            Quickplay.INSTANCE.chatBuffer.push("/whereami");

            // If a whereami message isnt found in 30 ticks, stop preparing to cancel
            new TickDelay(() -> this.cancelWhereamiMessages = false, 30);
        }
        return this;
    }

    /**
     * Get the latest instance if possible
     * @return The instance
     */
    public String getCurrentServer() {
        return instanceHistory.size() > 0 ? instanceHistory.get(0) : null;
    }
}
