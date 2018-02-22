package co.bugg.quickplay.http.response;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.config.ConfigSettings;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.util.Message;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
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
                // Overwrite settings
                Quickplay.INSTANCE.settings = new ConfigSettings();
                Quickplay.INSTANCE.keybinds = new ConfigKeybinds(true);
                // Overwrite file containing cached settings
                try {
                    Quickplay.INSTANCE.settings.save();
                    Quickplay.INSTANCE.keybinds.save();
                } catch (IOException e) {
                    System.out.println("Failed to save file while overwriting settings");
                    e.printStackTrace();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
                break;
            case RELOAD_GAMES:
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    System.out.println("Reloading games...");
                    // TODO send locale, POST parameter "locale" iirc
                    // TODO this might crash if there's a typo in the URL. Should double check.
                    WebResponse response = Quickplay.INSTANCE.requestFactory.newRequest(getValue().getAsString(), null).execute();

                    for (ResponseAction action : response.actions) {
                        action.run();
                    }

                    if(response.ok) {

                        Quickplay.INSTANCE.gameList = new ArrayList<>();
                        try {
                            response.content.getAsJsonObject().get("games").getAsJsonArray().forEach(
                                    obj -> Quickplay.INSTANCE.gameList.add(new Gson().fromJson(obj, Game.class)));

                            // Save the retrieved game list to cache
                            Quickplay.INSTANCE.assetFactory.saveCachedGameList(Quickplay.INSTANCE.gameList.toArray(new Game[Quickplay.INSTANCE.gameList.size()]));
                        } catch(Exception e) {
                            e.printStackTrace();
                            Quickplay.INSTANCE.sendExceptionRequest(e);
                        }

                        // Collect all icon URLs into a list and load them (if necessary)
                        Quickplay.INSTANCE.assetFactory.loadIcons(
                                Quickplay.INSTANCE.gameList.stream().map(game -> game.imageURL).collect(Collectors.toList()));
                    }
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
                    Quickplay.INSTANCE.sendExceptionRequest(e);
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
            case START_PING:
                Quickplay.INSTANCE.pingFrequency = value.getAsInt();

                // Stop previous ping thread if one exists
                if(Quickplay.INSTANCE.pingThread != null) Quickplay.INSTANCE.pingThread.cancel(true);
                // Start sending ping requests in a new thread
                Quickplay.INSTANCE.pingThread = Quickplay.INSTANCE.threadPool.submit(() -> {
                    while(Quickplay.INSTANCE.enabled && Quickplay.INSTANCE.pingFrequency > 0) {
                        try {
                            Thread.sleep(Quickplay.INSTANCE.pingFrequency * 1000);
                        } catch (InterruptedException e) {
                            System.out.println("Quickplay ping thread interrupted!");
                            break;
                        }

                        System.out.println("Pinging web server");
                        final Request request = Quickplay.INSTANCE.requestFactory.newPingRequest();
                        final WebResponse response = request.execute();

                        for(ResponseAction action : response.actions) {
                            action.run();
                        }
                    }
                });
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
