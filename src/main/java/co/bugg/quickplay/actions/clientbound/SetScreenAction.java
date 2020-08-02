package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Screen;
import co.bugg.quickplay.ScreenType;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.BufferUnderflowException;

public class SetScreenAction extends Action {
    @Override
    public void run() {
        try {

            final String availableOnJson = this.getPayloadObjectAsString(2);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);
            final String buttonsJson = this.getPayloadObjectAsString(4);
            final String[] buttonsArr = new Gson().fromJson(buttonsJson, String[].class);
            final String backButtonActionsJson = this.getPayloadObjectAsString(5);
            final String[] backButtonActionsArr = new Gson().fromJson(backButtonActionsJson, String[].class);

            final ScreenType screenType = ScreenType.valueOf(this.getPayloadObjectAsString(1));
            final String protocol = this.getPayloadObjectAsString(3);
            final String key = this.getPayloadObjectAsString(0);
            final String translationKey = this.getPayloadObjectAsString(6);

            final Screen screen = new Screen(key, screenType, availableOnArr, protocol, buttonsArr,
                    backButtonActionsArr, translationKey);

            Quickplay.INSTANCE.screenMap.put(key, screen);
        } catch (JsonSyntaxException | BufferUnderflowException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
