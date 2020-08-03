package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraft.util.ChatComponentText;

/**
 * Replacement for ChatComponentTranslation which sends translations through Quickplay's translator
 */
public class QuickplayChatComponentTranslation extends ChatComponentText {

    public QuickplayChatComponentTranslation(String key) {
        super(Quickplay.INSTANCE.translator.get(key));
    }

    public QuickplayChatComponentTranslation(String key, String... args) {
        super(Quickplay.INSTANCE.translator.get(key, args));
    }
}
