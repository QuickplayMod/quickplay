package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Button;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.BufferUnderflowException;

public class SetButtonAction extends Action {
    @Override
    public void run() {
        try {

            final String availableOnJson = this.getPayloadObjectAsString(1);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);
            final String actionsJson = this.getPayloadObjectAsString(3);
            final String[] actionsArr = new Gson().fromJson(actionsJson, String[].class);

            final String protocol = this.getPayloadObjectAsString(2);
            final String key = this.getPayloadObjectAsString(0);
            final String imageURL = this.getPayloadObjectAsString(4);
            final String translationKey = this.getPayloadObjectAsString(5);

            final Button button = new Button(key, availableOnArr, protocol, actionsArr, imageURL, translationKey);

            Quickplay.INSTANCE.buttonMap.put(key, button);
        } catch (JsonSyntaxException | BufferUnderflowException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
