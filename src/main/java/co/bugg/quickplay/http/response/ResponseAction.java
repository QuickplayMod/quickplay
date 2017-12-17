package co.bugg.quickplay.http.response;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.util.Message;
import com.google.gson.JsonElement;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * An action, instructed by a mod web endpoint
 */
public class ResponseAction {

    /**
     * Type of action this is
     */
    private ResponseActionType type;
    /**
     * Value of the action, class dependant
     * on the type of action this is
     */
    private JsonElement value;

    /**
     * Constructor
     * @param type Type of action
     * @param value Value of action
     */
    public ResponseAction(ResponseActionType type, JsonElement value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Run the action's instructions, depending on its type
     */
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
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    System.out.println("Reloading games...");
                    WebResponse response = Quickplay.INSTANCE.requestFactory.newRequest(getValue().getAsString(), null).execute();

                    for (ResponseAction action : response.actions) {
                        action.run();
                    }

                    Quickplay.INSTANCE.gameList = new ArrayList<>();
                    try {
                        response.content.getAsJsonObject().get("games").getAsJsonArray().forEach(
                                obj -> Quickplay.INSTANCE.gameList.add(WebResponse.GSON.fromJson(obj, Game.class)));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    // Collect all icon URLs into a list and load them
                    Quickplay.INSTANCE.assetFactory.loadIcons(
                            Quickplay.INSTANCE.gameList.stream().map(game -> game.imageURL).collect(Collectors.toList()));
                });
                break;
            case REFRESH_CACHE:
                // Get all files in the assets directory
                File[] files = new File(Quickplay.INSTANCE.assetFactory.assetsDirectory).listFiles();
                // If they exist
                if(files != null) {
                    for(File file : files) {
                        // Delete 'em all!!
                        file.delete();
                    }
                }
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

    /**
     * Getter for {@link #value}
     * @return {@link #value}
     */
    public JsonElement getValue() {
        return value;
    }

    /**
     * Getter for {@link #type}
     * @return {@link #type}
     */
    public ResponseActionType getType() {
        return type;
    }

    /**
     * Setter for {@link #type}
     * @param type new type
     */
    public void setType(ResponseActionType type) {
        this.type = type;
    }

    /**
     * Setter for {@link #value}
     * @param value new value
     */
    public void setValue(JsonElement value) {
        this.value = value;
    }
}
