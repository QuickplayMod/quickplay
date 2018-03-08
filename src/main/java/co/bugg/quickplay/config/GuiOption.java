package co.bugg.quickplay.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiOption {

    /**
     * Translation key for the name of this option
     * @return translation key for name
     */
    String name();

    /**
     * translation key for help text for this option
     * @return translation key for help text
     */
    String helpText();

    /**
     * Translation key for what category this setting belongs to
     * @return Category translation key
     */
    String category() default "quickplay.settings.category.general";

    /*
     * These settings are only used in integer and double options.
     */

    /**
     * Minimum value on a slider
     * @return Minimum value on the slider for this option
     */
    float minValue() default 0;

    /**
     * Maximum value on a slider
     * @return Maximum value on the slider for this option
     */
    float maxValue() default 255;

    /**
     * How to format this float when displaying it as a string
     * @return Format for {@link java.text.DecimalFormat}
     */
    String decimalFormat() default "0.00";

}
