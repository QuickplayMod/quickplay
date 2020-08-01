package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

public class SendChatCommandAction extends Action {
    @Override
    public void run() {
        String cmd = "/" + this.getPayloadObjectAsString(0);
        Quickplay.INSTANCE.chatBuffer.push(cmd);
    }
}
