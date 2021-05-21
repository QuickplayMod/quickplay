package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.wrappers.chat.ChatComponentTextWrapper;

/**
 * Replacement for ChatComponentTranslation which sends translations through Quickplay's translation system
 */
public class QuickplayChatComponentTranslation extends ChatComponentTextWrapper {

    public QuickplayChatComponentTranslation(String key) {
        super(Quickplay.INSTANCE.elementController.translate(key));
    }

    public QuickplayChatComponentTranslation(String key, String... args) {
        super(Quickplay.INSTANCE.elementController.translate(key, args));
    }
}
