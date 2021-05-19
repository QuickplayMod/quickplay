package co.bugg.quickplay;

import java.io.Serializable;

public class Element implements Serializable {
    public String key;
    public int elementType;

    public Element(String key, int elementType) {
        this.key = key;
        this.elementType = elementType;
    }
}
