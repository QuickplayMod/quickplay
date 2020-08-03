package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

public class SetTranslationAction extends Action {
    @Override
    public void run() {
        final String key = this.getPayloadObjectAsString(0);
        final String lang = this.getPayloadObjectAsString(1);
        final String val = this.getPayloadObjectAsString(2);

        Quickplay.INSTANCE.translator.set(key, lang, val);
    }
}
