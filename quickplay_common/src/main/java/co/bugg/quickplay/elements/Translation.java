package co.bugg.quickplay.elements;

import java.util.HashMap;

public class Translation extends Element {

    public HashMap<String, String> values = new HashMap<>();

    public Translation(String key) {
        super(key, 4);
    }

    public void setValueForLanguage(String language, String value) {
        this.values.put(language, value);
    }

    public String getValueForLanguage(String language) {
        return this.values.get(language);
    }

    public void removeValueForLanguage(String language) {
        this.values.remove(language);
    }

}
