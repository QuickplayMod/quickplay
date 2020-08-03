package co.bugg.quickplay.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Custom translations for Quickplay
 * Translations are sent over the socket connection, with the lang file used as a backup.
 */
public class ConfigTranslations extends AConfiguration implements Serializable {

    private final Map<String, Map<String, String>> translations = new HashMap<>();

    public static final String defaultLang = "en_US";

    /**
     * Constructor
     */
    public ConfigTranslations() {
        super("lang.json");
    }

    /**
     * Get the translation for the specified key
     * @param key Key of the translation to get
     * @param args Arguments provided to the translation
     * @return Formatted translation
     */
    public String get(final String key, String... args) {
        final Map<String, String> translationLangMap = this.translations.get(key);

        if(translationLangMap == null) {
            this.translations.put(key, new HashMap<>());
            return I18n.format(key, (Object[]) args);
        }

        String translation = translationLangMap.get(this.getCurrentLangKey());
        // Swap to default language if translation is null in current language
        if(translation == null) {
            translation = translationLangMap.get(ConfigTranslations.defaultLang);
        }
        if(translation == null) {
            return I18n.format(key, (Object[]) args);
        }

        try {
            return String.format(translation, (Object[]) args);
        } catch (IllegalFormatException e) {
            return "Format error: " + translationLangMap;
        }
    }

    /**
     * Set a translation key's value
     * @param key The key of the translation to set
     * @param lang The language to set this value in
     * @param val The translated string to set it to
     */
    public void set(final String key, final String lang, final String val) {
        Map<String, String> translationLangMap = this.translations.get(key);
        if(translationLangMap == null) {
            translationLangMap = new HashMap<>();
        }
        translationLangMap.put(lang, val);
        this.translations.put(key, translationLangMap);
    }

    /**
     * Get the current language used in Minecraft.
     * @return The language currently used by Minecraft.
     */
    private String getCurrentLangKey() {
        return Minecraft.getMinecraft().gameSettings.language;
    }
}
