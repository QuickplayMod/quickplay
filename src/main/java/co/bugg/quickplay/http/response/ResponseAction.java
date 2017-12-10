package co.bugg.quickplay.http.response;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class ResponseAction {

    private ResponseActionType type;
    private JsonElement value;

    public ResponseAction(ResponseActionType type, JsonElement value) {
        this.type = type;
        this.value = value;
    }

    public void run() {
        switch(type) {
            default:
            case SYSTEM_OUT:
                System.out.println(value.getAsString());
                break;
            case RESET_CONFIG:
                // TODO Reset all configuration options. Might be server-side?
                break;
            case RELOAD_GAMES:
                // TODO Reload the JSON for all games
                break;
            case REFRESH_CACHE:
                // TODO delete all cached game image files, etc.
                break;
            case SEND_MESSAGE:
                try {
                    Message message = Message.fromJson(value);
                    Quickplay.INSTANCE.messageBuffer.push(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DISABLE_MOD:
                final String reason = value.getAsString();
                System.out.println("Disabling mod, per web request guidelines. Reason: " + reason);
                Quickplay.INSTANCE.disable(reason);
                break;
            case ENABLE_MOD:
                System.out.println("Enabling mod, per web request guidelines. Reason: " + value.getAsString());
                Quickplay.INSTANCE.enable();
                break;
        }
    }

    public JsonElement getValue() {
        return value;
    }

    public ResponseActionType getType() {
        return type;
    }

    public void setType(ResponseActionType type) {
        this.type = type;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }
}
