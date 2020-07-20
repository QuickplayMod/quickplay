package co.bugg.quickplay.http.response;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.config.ConfigSettings;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.PartyMode;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.util.Message;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
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

                    HashMap<String, String> params = new HashMap<>();
                    Quickplay.INSTANCE.requestFactory.addStatisticsParameters(params);
                    params.put("locale", Minecraft.getMinecraft().gameSettings.language);
                    params.put("gamelistProtocol", "2");

                    WebResponse response = Quickplay.INSTANCE.requestFactory.newRequest(getValue().getAsString(), params)
                            .execute();

                    if(response != null) {
                        for (ResponseAction action : response.actions) {
                            action.run();
                        }

                        if (response.ok) {

                            Quickplay.INSTANCE.gameList = new ArrayList<>();
                            try {
                                response.content.getAsJsonObject().get("games").getAsJsonArray().forEach(
                                        obj -> Quickplay.INSTANCE.gameList.add(new Gson().fromJson(obj, Game.class)));


                                // Parse game list to verify the client's party mode list
                                if(Quickplay.INSTANCE.gameList.size() > 0) {
                                    // If the client has any party modes
                                    if (Quickplay.INSTANCE.settings.partyModes != null &&
                                            Quickplay.INSTANCE.settings.partyModes.size() > 0) {
                                        // Go through list of party modes the client has
                                        for (final ListIterator<PartyMode> iter = Quickplay.INSTANCE.settings.partyModes
                                                .listIterator(); iter.hasNext();) {
                                            final PartyMode mode = iter.next();
                                            // Split the namespace
                                            final String[] splitNamespace = mode.namespace.split("/");
                                            // If no games have the given unlocalized game name
                                            if(Quickplay.INSTANCE.gameList
                                                    .stream()
                                                    .noneMatch(game -> game.unlocalizedName.replace("/", "")
                                                            .equals(splitNamespace[0]))) {
                                                iter.remove();
                                            } else {
                                                boolean found = false;
                                                // For each game matching the first portion of the namespace
                                                for(Game gameMatchingNamespace : Quickplay.INSTANCE.gameList
                                                        .stream()
                                                        .filter(game -> game.unlocalizedName
                                                                .replace("/", "")
                                                                .equals(splitNamespace[0])).collect(Collectors.toList())) {
                                                    // If a mode with a command matching part two of the namespace is found, set found to true and break
                                                    if(gameMatchingNamespace.modes
                                                            .stream()
                                                            .anyMatch(mode1 -> mode1.command.replace("/", "")
                                                                    .equals(splitNamespace[1]))) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                // If a mode wasn't found, remove this party mode
                                                if(!found) {
                                                    iter.remove();
                                                }
                                            }
                                        }

                                        // Save after iteration is done
                                        Quickplay.INSTANCE.settings.save();
                                    }
                                } else {
                                    // There's no games so we know all party modes are invalid
                                    Quickplay.INSTANCE.settings.partyModes.clear();
                                    Quickplay.INSTANCE.settings.save();
                                }


                                // Save the retrieved game list to cache
                                Quickplay.INSTANCE.assetFactory.saveCachedGameList(Quickplay.INSTANCE.gameList.toArray(new Game[0]));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Quickplay.INSTANCE.sendExceptionRequest(e);
                            }

                            // Collect all icon URLs into a list and load them (if necessary)
                            Quickplay.INSTANCE.assetFactory.loadIcons(
                                    Quickplay.INSTANCE.gameList.stream().map(game -> game.imageURL).collect(Collectors.toList()));
                        }
                    }
                });
                break;
            case REFRESH_CACHE:
                Quickplay.INSTANCE.assetFactory.dumpAllCache();
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
                    // TODO URL should be sent from the web server as well
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

                        if(response != null) {
                            for (ResponseAction action : response.actions) {
                                action.run();
                            }
                        }
                        if(Quickplay.INSTANCE.ga != null) {
                            try {
                                Quickplay.INSTANCE.ga.createEvent("Systematic Events", "Ping")
                                        .setEventLabel(String.valueOf(Quickplay.INSTANCE.currentPing))
                                        .setEventValue(Quickplay.INSTANCE.currentPing)
                                        .send();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        // Increase ping count
                        Quickplay.INSTANCE.currentPing++;
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
