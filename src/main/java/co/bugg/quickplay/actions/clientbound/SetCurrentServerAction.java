package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

/**
 * Quickplay will support multiple servers. When the client reports a serverbound ServerJoinedAction, the server will
 * respond with this, saying what server it thinks the client is currently on, based on the information provided by the client.
 * This will correspond to actions, screens, etc. and what servers they are available on.
 */
public class SetCurrentServerAction extends Action {
    @Override
    public void run() {
        // TODO
    }
}
