package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the <code>/locraw</code> command on Hypixel and determining the client's location
 */
public class LocrawWrapper {
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
    final LocrawListenerCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback when this wrapper finds a /locraw message
     */
    public LocrawWrapper(LocrawListenerCallback callback) {

        Quickplay.INSTANCE.registerEventHandler(this);
        this.callback = callback;
        this.listening = true;
        this.cancel = true;

        // Send the /locraw command
        Quickplay.INSTANCE.chatBuffer.push("/locraw");
        System.out.println("QUICKPLAY DEBUG > Locraw sent! " + new Date().getTime() + " " + this.hashCode());
        // If a /locraw isn't received within 120 ticks (30 seconds), don't cancel the message
        new TickDelay(this::stopCancelling, 600);
        // If a /locraw isn't received within 1200 ticks (60 seconds), stop listening
        new TickDelay(() -> stopListening(null), 1200);
    }

    /**
     * Don't cancel the chat message if it comes
     * in, but still listen & call the callback
     */
    public void stopCancelling() {
        System.out.println("QUICKPLAY DEBUG > Cancelling stopped! " + new Date().getTime() + " " + this.hashCode());
        this.cancel = false;
    }

    /**
     * Stop listening for a chat message
     * and call the callback
     * @param instance Current instance to pass to callback
     */
    public void stopListening(String instance) {
        if(listening) {
            this.listening = false;
            Quickplay.INSTANCE.unregisterEventHandler(this);

            callback.call(instance);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        final String message = event.message.getUnformattedText();
        // Regex for the /locraw response
        final Pattern pattern = Pattern.compile("^\\{\"server\":");
        final Matcher matcher = pattern.matcher(message);

        if(
                Quickplay.INSTANCE.enabled &&
                Quickplay.INSTANCE.onHypixel &&
                matcher.find() &&
                listening
        ) {
            System.out.println("QUICKPLAY DEBUG > Locraw received! " + new Date().getTime() + " " + this.hashCode());

            if(this.cancel) {
                event.setCanceled(true);
            }

            try {
                String instance = null;
                final JsonObject locrawResponse = new Gson().fromJson(message, JsonObject.class);
                // Try lobby name first -- If null, use server name.
                if (locrawResponse.get("lobbyname") != null) {
                    instance = locrawResponse.get("lobbyname").getAsString();
                }
                if (instance == null && locrawResponse.get("server") != null) {
                    instance = locrawResponse.get("server").getAsString();
                }
                stopListening(instance);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                stopListening(null);
            }
        }

    }

    /**
     * Interface for inline callbacks
     * Called when a response to /locraw is received,
     * or after 60 seconds of no response with "null" passed
     */
    @FunctionalInterface
    public interface LocrawListenerCallback {

        void call(String instance);
    }
}
