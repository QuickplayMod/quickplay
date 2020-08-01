package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

public class DisableModAction extends Action {
    @Override
    public void run() {
        final String reason = this.getPayloadObjectAsString(0);
        Quickplay.INSTANCE.disable(reason);
    }
}
