package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.AliasedAction;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Location;
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

            final Gson gson = new Gson();
            final ByteBuffer builtAction = this.getPayloadObject(2);
            final Action action = Action.from(builtAction);

            final String availableOnJson = this.getPayloadObjectAsString(1);
            final String[] availableOnArr = gson.fromJson(availableOnJson, String[].class);

            final String key = this.getPayloadObjectAsString(0);

            final ByteBuffer visibleBuf = this.getPayloadObject(3);
            final boolean visible = visibleBuf.get() != (byte) 0;
            final ByteBuffer adminOnlyBuf = this.getPayloadObject(4);
            final boolean adminOnly = adminOnlyBuf.get() != (byte) 0;
            final String hypixelLocrawRegexJson = this.getPayloadObjectAsString(5);
            final Location hypixelLocrawRegex = gson.fromJson(hypixelLocrawRegexJson, Location.class);
            final String hypixelRankRegex = this.getPayloadObjectAsString(6);
            final String hypixelPackageRankRegex = this.getPayloadObjectAsString(7);
            final ByteBuffer hypixelBuildTeamOnlyBuf = this.getPayloadObject(8);
            final boolean hypixelBuildTeamOnly = hypixelBuildTeamOnlyBuf.get() != (byte) 0;
            final ByteBuffer hypixelBuildTeamAdminOnlyBuf = this.getPayloadObject(9);
            final boolean hypixelBuildTeamAdminOnly = hypixelBuildTeamAdminOnlyBuf.get() != (byte) 0;

            final AliasedAction aliasedAction = new AliasedAction(key, availableOnArr, action, visible,
                    adminOnly, hypixelLocrawRegex, hypixelRankRegex, hypixelPackageRankRegex, hypixelBuildTeamOnly,
                    hypixelBuildTeamAdminOnly);

            Quickplay.INSTANCE.aliasedActionMap.put(key, aliasedAction);
        } catch (JsonSyntaxException | BufferUnderflowException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
