package co.bugg.quickplay;

import co.bugg.quickplay.actions.Action;

public class AliasedAction {
    public final String key;
    public final String protocol;
    public final String[] availableOn;
    public final Action action;

    public AliasedAction(final String key, final String[] availableOn, final String protocol, final Action action) {
        this.key = key;
        this.availableOn = availableOn;
        this.protocol = protocol;
        this.action = action;
    }
}
