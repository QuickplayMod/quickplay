package co.bugg.quickplay.actions;

import co.bugg.quickplay.actions.clientbound.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actions are the core mechanism behind how Quickplay operates. Whenever the client/user
 * clicks a button, presses a keybind, receives instructions from the web server, etc.,
 * Besides the current commands system, the client is not able to do any I/O other than what
 * is available through Actions (eventually, ideally the Quickplay commands would also run Actions).
 *
 * Actions are serializable in a similar format to Minecraft packets, and can be sent over the wire.
 * The structure is as follows:
 * When serialized, all Actions must contain at least 2 bytes. These are the first two bytes, which
 * are the Action's ID. All subsequent bytes are considered the payload. They can be considered arguments
 * to the Action, and are split up into partitions, each of which is one argument. An argument begins
 * with the first 4 bytes being the length x of the argument. After those bytes, the next x bytes are
 * the actual argument. This signature repeats, until there are no more bytes.
 *
 * If there are too few bytes in the Action, a BufferUnderflowException will be thrown. It is possible
 * for a serialized Action to be valid, but the subsequent execution of the Action to fail if there were
 * not enough arguments provided in the payload.
 *
 * Actions can also be sent to the web server, providing context to actions/events occurring on the client,
 * such as exceptions, connection status, etc.
 */
public abstract class Action {

    /**
     * A map mapping action IDs to their classes
     */
    private static final Map<Short, Class<? extends Action>> actionIdToActionClass = new HashMap<>();
    static {
        actionIdToActionClass.put((short) 0, Action.class);
        actionIdToActionClass.put((short) 1, EnableModAction.class);
        actionIdToActionClass.put((short) 2, DisableModAction.class);
        actionIdToActionClass.put((short) 3, SendChatComponentAction.class);
        actionIdToActionClass.put((short) 4, SystemOutAction.class);
        actionIdToActionClass.put((short) 5, ResetConfigAction.class);
        actionIdToActionClass.put((short) 6, SendChatCommandAction.class);
    }

    /**
     * The ID of this action
     */
    protected short id;
    /**
     * The items in the payload, stored as raw ByteBuffers.
     * @see this#getPayloadObjectAsString(int)
     */
    protected final List<ByteBuffer> payloadObjs;

    public Action () {
        this.id = 0;
        this.payloadObjs = new ArrayList<>();
    }

    /**
     * Deserialize a ByteBuffer into an Action.
     * Strings are assumed to be UTF-8, and bytes to be in big endian order.
     * @param in Incoming ByteBuffer to decode.
     * @return The newly decoded Action
     * @throws BufferUnderflowException There are not enough bytes to be a valid Action.
     * @throws IllegalAccessException The Action with the provided ID could not be instantiated.
     * @throws InstantiationException The Action with the provided ID could not be instantiated.
     */
    public static Action from(ByteBuffer in) throws BufferUnderflowException, IllegalAccessException, InstantiationException {
        if(in.remaining() < 2) {
            throw new BufferUnderflowException();
        }

        // Get the ID
        short id = in.getShort();
        // From that ID, find the Action class and instantiate it, if possible.
        final Action action = actionIdToActionClass.get(id).newInstance();
        // Move forward 2 bytes
        in.position(2);

        // Decode payload until the end of the Action is reached.
        while(in.hasRemaining()) {
            int currentPos = in.position();

            int length = in.getInt();
            in.position(currentPos + 4);
            currentPos += 4;

            byte[] payload = new byte[length];
            in.get(payload, 0, length);
            in.position(currentPos + length);
            action.addPayload(ByteBuffer.wrap(payload));
        }

        return action;
    }

    /**
     * Get the total number of payload items.
     * @return Number of items in {@link this#payloadObjs}
     */
    int payloadCount() {
        return this.payloadObjs.size();
    }

    /**
     * Get an item from the Payload and convert it to a String in UTF-8.
     * @param index Index of the item to get. Must be >= 0 and < payloadObjs.size()
     * @return Decoded String
     */
    protected String getPayloadObjectAsString(int index) {
        return StandardCharsets.UTF_8.decode(this.payloadObjs.get(index)).toString();
    }

    /**
     * This method can be called to run the implementation of whatever this Action is
     * supposed to do.
     */
    public abstract void run();

    /**
     * Add an item to the payload of this Action.
     * @param payload The payload item to add.
     */
    public void addPayload(ByteBuffer payload) {
        this.payloadObjs.add(payload);
    }
}
