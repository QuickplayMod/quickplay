package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Message;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class SendChatComponentAction extends Action {
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
