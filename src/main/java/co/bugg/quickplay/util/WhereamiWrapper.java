package co.bugg.quickplay.util;

import cc.hyperium.Hyperium;
import cc.hyperium.event.InvokeEvent;
import cc.hyperium.event.network.chat.ChatEvent;
import co.bugg.quickplay.Quickplay;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the <code>/whereami</code> command on Hypixel and determining the client's location
 */
public class WhereamiWrapper {
    /**
     * Whether this wrapper should listen for & action on chat messages
     */
    boolean listening;
    /**
     * Whether this wrapper should cancel whereami messages it finds
     */
    boolean cancel;
    /**
     * Callback when this wrapper finds a /whereami message
     */
    final WhereamiListenerCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback when this wrapper finds a /whereami message
     */
    public WhereamiWrapper(WhereamiListenerCallback callback) {

        Quickplay.INSTANCE.registerEventHandler(this);
        this.callback = callback;
        this.listening = true;
        this.cancel = true;

        // Send the /whereami command
        Quickplay.INSTANCE.chatBuffer.push("/whereami");
        // If a /whereami isn't received within 120 ticks (6 seconds), don't cancel the message
        new TickDelay(this::stopCancelling, 120);
        // If a /whereami isn't received within 1200 ticks (60 seconds), stop listening
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

    @InvokeEvent
    public void onChat(ChatEvent event) {
        final String message = event.getChat().getUnformattedText();
        // Regex for the /whereami response
        // §bYou are currently connected to server §r§6lobby5§r
        final Pattern pattern = Pattern.compile("^You are currently (?:(?:in |connected to server )(limbo|(?:(?:[A-Za-z]+)?lobby(?:\\d{1,3})|(?:mega|mini)\\d{1,3}[A-Z])))$");
        final Matcher matcher = pattern.matcher(message);

        if(
                Quickplay.INSTANCE.enabled &&
                Hyperium.INSTANCE.getHandlers().getHypixelDetector().isHypixel() &&
                !event.isCancelled() &&
                matcher.find() &&
                listening
        ) {

            if(this.cancel)
                event.setCancelled(true);

            // Get the regex group containing the current instance
            final String instance = matcher.group(1);
            stopListening(instance);
        }

    }

    /**
     * Interface for inline callbacks
     * Called when a response to /whereami is received,
     * or after 60 seconds of no response with "null" passed
     */
    @FunctionalInterface
    public interface WhereamiListenerCallback {

        void call(String instance);
    }
}
