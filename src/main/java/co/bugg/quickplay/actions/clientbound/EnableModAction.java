package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;

import java.net.URISyntaxException;

/**
 * ID: 1
 * Enable the Quickplay mod for the receiving client.
 */
public class EnableModAction extends Action {

    public EnableModAction() {
        super();
        this.id = 1;
    }

    @Override
    public void run() {
        try {
            Quickplay.INSTANCE.enable();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation("quickplay.failedToEnable"),
                    true, true));
        }
    }
}
