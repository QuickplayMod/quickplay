package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.wrappers.chat.IChatComponentWrapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ID: 16
 * Set the Quickplay Premium about text.
 *
 * Payload Order:
 * about text chat component
 */
public class SetPremiumAboutAction extends Action {

    public SetPremiumAboutAction() {}

    /**
     * Create a new SetPremiumAboutAction.
     * @param component Chat component to set the about text to.
     */
    public SetPremiumAboutAction(IChatComponentWrapper component) {
        super();
        this.id = 16;
        this.addPayload(ByteBuffer.wrap(IChatComponentWrapper.serialize(component).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.premiumAbout = IChatComponentWrapper.deserialize(this.getPayloadObjectAsString(0));
    }
}
