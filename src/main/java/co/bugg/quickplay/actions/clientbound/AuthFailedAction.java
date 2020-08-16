package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

/**
 * ID: 39
 * Notify the client that authentication failed, doesn't specify the reason.
 */
public class AuthFailedAction extends Action {

    /**
     * Create a new AuthFailedAction.
     */
    public AuthFailedAction() {
        super();
        this.id = 39;
    }

    @Override
    public void run() {
        // TODO
    }
}
