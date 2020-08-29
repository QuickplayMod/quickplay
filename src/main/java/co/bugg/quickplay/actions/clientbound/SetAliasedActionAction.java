package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.AliasedAction;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * ID: 7
 * Set an aliased action in the client with the provided key and parameters.
 *
 * Payload Order:
 * key
 * availableOn JSON array
 * The Action built as normal
 */
public class SetAliasedActionAction extends Action {

    public SetAliasedActionAction() {}

    /**
     * Create a new SetAliasedActionAction.
     * @param aliasedAction Aliased action to be saved to the client.
     */
    public SetAliasedActionAction(AliasedAction aliasedAction) {
        super();
        this.id = 7;
        this.addPayload(ByteBuffer.wrap(aliasedAction.key.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(aliasedAction.availableOn).getBytes()));
        this.addPayload(aliasedAction.action.build());
    }

    @Override
    public void run() {
        try {
            final ByteBuffer builtAction = this.getPayloadObject(2);
            final Action action = Action.from(builtAction);

            final String availableOnJson = this.getPayloadObjectAsString(1);
            final String[] availableOnArr = new Gson().fromJson(availableOnJson, String[].class);

            final String key = this.getPayloadObjectAsString(0);

            final ByteBuffer adminOnlyBuf = this.getPayloadObject(3);
            final boolean adminOnly = adminOnlyBuf.get() != (byte) 0;

            final AliasedAction aliasedAction = new AliasedAction(key, availableOnArr, action, adminOnly);

            Quickplay.INSTANCE.aliasedActionMap.put(key, aliasedAction);
        } catch (JsonSyntaxException | BufferUnderflowException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
