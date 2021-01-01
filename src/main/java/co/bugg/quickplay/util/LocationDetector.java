package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the <code>/locraw</code> command on Hypixel and determining the client's location
 */
public class LocationDetector {
    /**
     * Whether this wrapper should listen for & action on chat messages
     */
    boolean listening;
    /**
     * Whether this wrapper should cancel locraw messages it finds
     */
    boolean cancel;
    /**
     * Callback when this wrapper finds a /locraw message
     */
    final LocationListenerCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback when this wrapper finds a /locraw message
     */
    public LocationDetector(LocationListenerCallback callback) {

        Quickplay.INSTANCE.registerEventHandler(this);
        this.callback = callback;
        this.listening = true;
        this.cancel = true;

        // Send the /locraw command
        Quickplay.INSTANCE.chatBuffer.push("/locraw");
        // If a /locraw isn't received within 300 ticks (15 seconds), don't cancel the message
        new TickDelay(this::stopCancelling, 300);
        // If a /locraw isn't received within 1200 ticks (60 seconds), stop listening
        new TickDelay(() -> stopListening(null), 1200);
    }

    /**
     * Don't cancel the chat message if it comes
     * in, but still listen & call the callback
     */
    public void stopCancelling() {
        this.cancel = false;
    }

    /**
     * Stop listening for a chat message and call the callback
     * @param location Current location to pass to the callback.
     */
    public void stopListening(Location location) {
        if(listening) {
            this.listening = false;
            Quickplay.INSTANCE.unregisterEventHandler(this);

            callback.call(location);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        final String message = event.message.getUnformattedText();
        // Regex to detect /locraw messages. All locraw messages are JSON starting with {"server":...
        final Pattern pattern = Pattern.compile("^\\{\"server\":");
        final Matcher matcher = pattern.matcher(message);

        if(
                Quickplay.INSTANCE.isEnabled &&
                Quickplay.INSTANCE.isOnHypixel() &&
                !event.isCanceled() &&
                matcher.find() &&
                this.listening
        ) {

            Location location;
            try {
                location = new Gson().fromJson(message, Location.class);
            } catch(JsonSyntaxException e) {
                return;
            }

            if(this.cancel) {
                event.setCanceled(true);
            }

            // Get the current instance.
            this.stopListening(location);
        }

    }

    /**
     * Interface for inline callbacks
     * Called when a response to /locraw is received,
     * or after 60 seconds of no response with "null" passed
     */
    @FunctionalInterface
    public interface LocationListenerCallback {

        void call(Location location);
    }
}
