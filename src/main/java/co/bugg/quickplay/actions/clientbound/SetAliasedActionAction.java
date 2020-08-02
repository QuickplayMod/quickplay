package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.AliasedAction;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class SetAliasedActionAction extends Action {
    @Override
    public void run() {
        try {
            final ByteBuffer builtAction = this.payloadObjs.get(3);
            final Action action = Action.from(builtAction);

            final String availableOnJson = this.getPayloadObjectAsString(1);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);

            final String protocol = this.getPayloadObjectAsString(2);
            final String key = this.getPayloadObjectAsString(0);

            final AliasedAction aliasedAction = new AliasedAction(key, availableOnArr, protocol, action);

            Quickplay.INSTANCE.aliasedActionMap.put(key, aliasedAction);
        } catch (JsonSyntaxException | BufferUnderflowException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
