package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

/**
 * ID: 24
 * Received by the server when the client disconnects from the server they were previously on.
 */
public class ServerLeftAction extends Action {

    /**
     * Create a new ServerLeftAction.
     */
    public ServerLeftAction() {
        super();
        this.id = 24;
    }
}
