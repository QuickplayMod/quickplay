package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.config.ConfigSettings;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 51
 * Received by the server from the client when the client wishes to notify
 * the server of it's current settings. This would typically be at initialization,
 * or whenever the user changes their settings.
 *
 * Settings are received by the server in order to allow for the server to
 * be able to send different data depending on the client's settings.
 *
 * Payload Order:
 * Settings data JSON
 */
public class SetClientSettingsAction extends Action {

    public SetClientSettingsAction () {}

    /**
     * Create a new ServerJoinedAction.
     * @param settingsData The client's settings object.
     */
    public SetClientSettingsAction(ConfigSettings settingsData) {
        super();
        this.id = 51;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(settingsData).getBytes()));
    }
}
