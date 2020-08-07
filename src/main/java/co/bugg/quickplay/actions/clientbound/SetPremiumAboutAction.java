package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import net.minecraft.util.IChatComponent;

import java.nio.ByteBuffer;

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
    public SetPremiumAboutAction(IChatComponent component) {
        super();
        this.id = 16;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(component).getBytes()));
    }

    @Override
    public void run() {
        // TODO
    }
}
