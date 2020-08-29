package co.bugg.quickplay;

import co.bugg.quickplay.actions.Action;

public class AliasedAction {
    public final String key;
    public final String[] availableOn;
    public final Action action;
    public final boolean adminOnly;

    public AliasedAction(final String key, final String[] availableOn, final Action action, final boolean adminOnly) {
        this.key = key;
        this.availableOn = availableOn;
        this.action = action;
        this.adminOnly = adminOnly;
    }
}
