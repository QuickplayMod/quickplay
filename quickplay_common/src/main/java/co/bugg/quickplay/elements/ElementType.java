package co.bugg.quickplay.elements;

public enum ElementType {
    SCREEN(1),
    BUTTON(2),
    ALIASED_ACTION(3),
    TRANSLATION(4),
    REGULAR_EXPRESSION(5);

    private final int value;
    ElementType(int value) {
        this.value = value;
    }

    int getValue() {
        return this.value;
    }
}
