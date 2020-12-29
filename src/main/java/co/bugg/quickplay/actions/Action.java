package co.bugg.quickplay.actions;

import co.bugg.quickplay.actions.clientbound.*;
import co.bugg.quickplay.actions.serverbound.*;

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
 * such as exceptions, connection status, button presses, etc.
 */
public class Action {

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
        actionIdToActionClass.put((short) 7, SetAliasedActionAction.class);
        actionIdToActionClass.put((short) 8, SetButtonAction.class);
        actionIdToActionClass.put((short) 9, SetScreenAction.class);
        actionIdToActionClass.put((short) 10, OpenGuiAction.class);
        actionIdToActionClass.put((short) 11, OpenScreenAction.class);
        actionIdToActionClass.put((short) 12, RefreshCacheAction.class);
        actionIdToActionClass.put((short) 13, SetCurrentServerAction.class);
        actionIdToActionClass.put((short) 14, SetGlyphForUserAction.class);
        actionIdToActionClass.put((short) 15, SetKeybindsAction.class);
        actionIdToActionClass.put((short) 16, SetPremiumAboutAction.class);
        actionIdToActionClass.put((short) 17, SetTranslationAction.class);
        actionIdToActionClass.put((short) 18, ButtonPressedAction.class);
        actionIdToActionClass.put((short) 19, ExceptionThrownAction.class);
        actionIdToActionClass.put((short) 20, HypixelLocationChangedAction.class);
        actionIdToActionClass.put((short) 21, MigrateKeybindsAction.class);
        actionIdToActionClass.put((short) 22, LanguageChangedAction.class);
        actionIdToActionClass.put((short) 23, ServerJoinedAction.class);
        actionIdToActionClass.put((short) 24, ServerLeftAction.class);
        actionIdToActionClass.put((short) 25, InitializeClientAction.class);
        actionIdToActionClass.put((short) 26, AuthBeginHandshakeAction.class);
        actionIdToActionClass.put((short) 27, AuthMojangEndHandshakeAction.class);
        actionIdToActionClass.put((short) 28, AuthCompleteAction.class);
        actionIdToActionClass.put((short) 29, AuthGoogleEndHandshakeAction.class);
        actionIdToActionClass.put((short) 30, DeleteScreenAction.class);
        actionIdToActionClass.put((short) 31, DeleteButtonAction.class);
        actionIdToActionClass.put((short) 32, DeleteAliasedActionAction.class);
        actionIdToActionClass.put((short) 33, AlterScreenAction.class);
        actionIdToActionClass.put((short) 34, AlterButtonAction.class);
        actionIdToActionClass.put((short) 35, AlterAliasedActionAction.class);
        actionIdToActionClass.put((short) 36, SetCurrentUserCountAction.class);
        actionIdToActionClass.put((short) 37, SetUserCountHistoryAction.class);
        actionIdToActionClass.put((short) 38, AuthReestablishAuthedConnectionAction.class);
        actionIdToActionClass.put((short) 39, AuthFailedAction.class);
        actionIdToActionClass.put((short) 40, RemoveScreenAction.class);
        actionIdToActionClass.put((short) 41, RemoveButtonAction.class);
        actionIdToActionClass.put((short) 42, RemoveAliasedActionAction.class);
        actionIdToActionClass.put((short) 43, AlterTranslationAction.class);
        actionIdToActionClass.put((short) 44, DeleteTranslationAction.class);
        actionIdToActionClass.put((short) 45, RemoveTranslationAction.class);
        actionIdToActionClass.put((short) 46, AlterGlyphAction.class);
        actionIdToActionClass.put((short) 47, GetDailyRewardAction.class);
        actionIdToActionClass.put((short) 48, SetDailyRewardDataAction.class);
        actionIdToActionClass.put((short) 49, ClaimDailyRewardAction.class);
        actionIdToActionClass.put((short) 50, PushEditHistoryEventAction.class);
        actionIdToActionClass.put((short) 51, SetClientSettingsAction.class);
    }

    /**
     * The ID of this action
     */
    protected short id;
    /**
     * The items in the payload, stored as raw ByteBuffers.
     * @see this#getPayloadObjectAsString(int)
     */
    private final List<ByteBuffer> payloadObjs;

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
    public int payloadCount() {
        return this.payloadObjs.size();
    }

    /**
     * Get an object from the payload at the specified index
     * @param index Index of the item to get. Should be >= 0 and < {@link #payloadCount()}
     * @return The payload item, or null if it does not exist.
     */
    public ByteBuffer getPayloadObject(int index) {
        final ByteBuffer obj = this.payloadObjs.get(index);
        if(obj == null) {
            return null;
        }
        obj.rewind();
        return obj;
    }

    /**
     * Get an item from the Payload and convert it to a String in UTF-8.
     * @param index Index of the item to get. Must be >= 0 and < payloadObjs.size()
     * @return Decoded String
     */
    protected String getPayloadObjectAsString(int index) {

        return StandardCharsets.UTF_8.decode(this.getPayloadObject(index)).toString();
    }

    /**
     * This method can be called to run the implementation of whatever this Action is
     * supposed to do. Should be overridden for clientbound actions.
     */
    public void run() {}

    /**
     * Build an action into a Buffer from its ID and payload list.
     * @return {Buffer} Built buffer which can be sent over the wire.
     */
    public ByteBuffer build() {
        ByteBuffer body = ByteBuffer.allocate(2);
        body.putShort(this.id);

        for (ByteBuffer payload : this.payloadObjs) {
            ByteBuffer payloadSizeBuf = ByteBuffer.allocate(4);
            int payloadSize = payload.rewind().remaining();
            int bodySize = body.rewind().remaining();

            payloadSizeBuf.putInt(payloadSize).rewind();
            ByteBuffer newBody = ByteBuffer.allocate(bodySize + 4 + payloadSize);
            newBody.put(body).put(payloadSizeBuf).put(payload);

            body = newBody;
        }

        body.rewind();
        return body;
    }

    /**
     * Add an item to the payload of this Action.
     * @param payload The payload item to add.
     */
    public void addPayload(ByteBuffer payload) {
        this.payloadObjs.add(payload);
    }

    /**
     * Add a String to the payload.
     * @param str String to add to the payload
     * @param defaultValue If str is null, this value will be added instead.
     */
    public void addPayloadString(String str, String defaultValue) {
        if(str == null) {
            this.addPayload(ByteBuffer.wrap(defaultValue.getBytes()));
        } else {
            this.addPayload(ByteBuffer.wrap(str.getBytes()));
        }
    }

    /**
     * Add a boolean value to the payload. Boolean values take up 1 byte instead of 1 bit,
     * as that's the atomic value of the Quickplay protocol.
     * @param b Boolean to add.
     */
    public void addPayloadBoolean(boolean b) {
        ByteBuffer boolBuf = ByteBuffer.allocate(1);
        boolBuf.put(b ? (byte) 1 : (byte) 0);
        boolBuf.rewind();
        this.addPayload(boolBuf);
    }

    /**
     * Add an integer value to the payload. Integers take up 4 bytes.
     * @param i Integer to add.
     */
    public void addPayloadInteger(int i) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(i);
        buf.rewind();
        this.addPayload(buf);
    }
}
