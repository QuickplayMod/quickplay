package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Screen;
import co.bugg.quickplay.ScreenType;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * ID: 9
 * Set a screen in the client with the provided key and parameters.
 *
 * Payload Order:
 * key
 * screenType
 * availableOn JSON array
 * buttons JSON array of button keys
 * backButtonActions JSON array of aliased action keys which execute when the back button is pressed
 * translationKey
 * imageURL
 */
public class SetScreenAction extends Action {

    public SetScreenAction() {}

    /**
     * Create a new SetScreenAction.
     * @param screen Screen to be saved to the client.
     */
    public SetScreenAction(Screen screen) {
        super();
        this.id = 9;
        this.addPayload(ByteBuffer.wrap(screen.key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(screen.availableOn).getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(screen.buttonKeys).getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(screen.backButtonActions).getBytes()));
        this.addPayload(ByteBuffer.wrap(screen.translationKey.getBytes()));
        this.addPayload(ByteBuffer.wrap(screen.imageURL.getBytes()));
    }

    @Override
    public void run() {
        try {

            final String availableOnJson = this.getPayloadObjectAsString(2);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);
            final String buttonsJson = this.getPayloadObjectAsString(3);
            final String[] buttonsArr = new Gson().fromJson(buttonsJson, String[].class);
            final String backButtonActionsJson = this.getPayloadObjectAsString(4);
            final String[] backButtonActionsArr = new Gson().fromJson(backButtonActionsJson, String[].class);

            final ScreenType screenType = ScreenType.valueOf(this.getPayloadObjectAsString(1));
            final String key = this.getPayloadObjectAsString(0);
            final String translationKey = this.getPayloadObjectAsString(5);
            final String imageURL = this.getPayloadObjectAsString(6);

            final Screen screen = new Screen(key, screenType, availableOnArr, buttonsArr, backButtonActionsArr,
                    translationKey, imageURL);

            Quickplay.INSTANCE.screenMap.put(key, screen);
        } catch (JsonSyntaxException | BufferUnderflowException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
