package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

/**
 * ID: 12
 * Delete all cached files/images/etc on the client and force them to be recreated.
 * Does not delete session cache (e.g. screen list), but cache that persists across
 * sessions (e.g. directory with Glyph images).
 */
public class RefreshCacheAction extends Action {

    /**
     * Create a new RefreshCacheAction.
     */
    public RefreshCacheAction() {
        super();
        this.id = 12;
    }

    @Override
    public void run() {
        // TODO
    }
}
