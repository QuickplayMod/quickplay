package co.bugg.quickplay;

public class RegularExpression extends Element {

    public String value;

    public RegularExpression(String key, String value) {
        super(key, 5);
        this.value = value;
    }
}
