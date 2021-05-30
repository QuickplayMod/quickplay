package co.bugg.quickplay.elements;

public class RegularExpression extends Element {

    public String value;

    public RegularExpression(String key, String value) {
        super(key, ElementType.REGULAR_EXPRESSION.getValue());
        this.value = value;
    }
}
