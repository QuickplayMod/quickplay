package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Button;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * ID: 8
 * Set a button in the client with the provided key and parameters.
 *
 * Payload Order:
 * key
 * availableOn JSON array
 * actions JSON array of aliased action keys
 * imageURL
 * translationKey
 */
public class SetButtonAction extends Action {

    public SetButtonAction() {}

    /**
     * Create a new SetButtonAction.
     * @param button Button to be saved to the client.
     */
    public SetButtonAction(Button button) {
        super();
        this.id = 8;
        this.addPayload(ByteBuffer.wrap(button.key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(button.availableOn).getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(button.actionKeys).getBytes()));
        this.addPayload(ByteBuffer.wrap(button.imageURL.getBytes()));
        this.addPayload(ByteBuffer.wrap(button.translationKey.getBytes()));

    }

    @Override
    public void run() {
        try {

            final String availableOnJson = this.getPayloadObjectAsString(1);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);
            final String actionsJson = this.getPayloadObjectAsString(2);
            final String[] actionsArr = new Gson().fromJson(actionsJson, String[].class);

            final String key = this.getPayloadObjectAsString(0);
            final String imageURL = this.getPayloadObjectAsString(3);
            final String translationKey = this.getPayloadObjectAsString(4);
            final ByteBuffer adminOnlyBuf = this.getPayloadObject(5);
            final boolean adminOnly = adminOnlyBuf.get() != (byte) 0;

            final Button button = new Button(key, availableOnArr, actionsArr, imageURL, translationKey, adminOnly);

            // Download the image URL, if it is set
            if(button.imageURL != null && button.imageURL.length() > 0) {
                try {
                    Quickplay.INSTANCE.assetFactory.loadIcon(new URL(button.imageURL));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            Quickplay.INSTANCE.buttonMap.put(key, button);
        } catch (JsonSyntaxException | BufferUnderflowException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
