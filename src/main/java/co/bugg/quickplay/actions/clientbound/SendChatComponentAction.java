package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Message;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.IChatComponent;

import java.nio.ByteBuffer;

/**
 * ID: 3
 * Send a Quickplay chat message to the client's chat.
 */
public class SendChatComponentAction extends Action {

    public SendChatComponentAction() {}

    /**
     * Create a new SendChatComponentAction.
     * @param chatComponent Chat component message for the client to send
     */
    public SendChatComponentAction(IChatComponent chatComponent) {
        super();
        this.id = 3;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(chatComponent).getBytes()));
    }

    @Override
    public void run() {
        try {
            final JsonElement elem = new JsonParser().parse(this.getPayloadObjectAsString(0));
            Message message = Message.fromJson(elem);
            Quickplay.INSTANCE.messageBuffer.push(message);
        } catch (JsonParseException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
