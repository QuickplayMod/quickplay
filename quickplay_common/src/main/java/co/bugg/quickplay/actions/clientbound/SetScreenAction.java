package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.elements.Screen;
import co.bugg.quickplay.elements.ScreenType;
import co.bugg.quickplay.util.Location;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.MalformedURLException;
import java.net.URL;
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

            final Gson gson = new Gson();
            final String availableOnJson = this.getPayloadObjectAsString(2);
            final String[] availableOnArr = gson.fromJson(availableOnJson, String[].class);
            final String buttonsJson = this.getPayloadObjectAsString(3);
            final String[] buttonsArr = gson.fromJson(buttonsJson, String[].class);
            final String backButtonActionsJson = this.getPayloadObjectAsString(4);
            final String[] backButtonActionsArr = gson.fromJson(backButtonActionsJson, String[].class);

            final ScreenType screenType = ScreenType.valueOf(this.getPayloadObjectAsString(1));
            final String key = this.getPayloadObjectAsString(0);
            final String translationKey = this.getPayloadObjectAsString(5);
            final String imageURL = this.getPayloadObjectAsString(6);
            final ByteBuffer visibleBuf = this.getPayloadObject(7);
            final boolean visible = visibleBuf.get() != (byte) 0;
            final ByteBuffer adminOnlyBuf = this.getPayloadObject(8);
            final boolean adminOnly = adminOnlyBuf.get() != (byte) 0;
            final String hypixelLocrawRegexJson = this.getPayloadObjectAsString(9);
            final Location hypixelLocrawRegex = gson.fromJson(hypixelLocrawRegexJson, Location.class);
            final String hypixelRankRegex = this.getPayloadObjectAsString(10);
            final String hypixelPackageRankRegex = this.getPayloadObjectAsString(11);
            final ByteBuffer hypixelBuildTeamOnlyBuf = this.getPayloadObject(12);
            final boolean hypixelBuildTeamOnly = hypixelBuildTeamOnlyBuf.get() != (byte) 0;
            final ByteBuffer hypixelBuildTeamAdminOnlyBuf = this.getPayloadObject(13);
            final boolean hypixelBuildTeamAdminOnly = hypixelBuildTeamAdminOnlyBuf.get() != (byte) 0;

            final Screen screen = new Screen(key, screenType, availableOnArr, buttonsArr, backButtonActionsArr,
                    translationKey, imageURL, visible, adminOnly, hypixelLocrawRegex, hypixelRankRegex,
                    hypixelPackageRankRegex, hypixelBuildTeamOnly, hypixelBuildTeamAdminOnly);

            // Download the image URL, if it is set
            if(screen.imageURL != null && screen.imageURL.length() > 0) {
                try {
                    Quickplay.INSTANCE.assetFactory.loadIcon(new URL(screen.imageURL));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            Quickplay.INSTANCE.elementController.putElement(screen);
        } catch (JsonSyntaxException | BufferUnderflowException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
