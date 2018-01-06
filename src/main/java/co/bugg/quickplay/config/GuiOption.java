package co.bugg.quickplay.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiOption {
    String name();
    String helpText();
    String category() default "General";
    /*
     * These settings are only used in integer and double options.
     */
    float minValue() default 0;
    float maxValue() default 255;
    int precision() default 2;

}
