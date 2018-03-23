package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.config.GuiOption;

/**
 * Element for the Quickplay Configuration GUIs
 * Used for storing {@link GuiOption} annotations with their
 * respective config element if necessary
 */
public class ConfigElement {
    public static final int ELEMENT_HEIGHT = 20;
    public static final int ELEMENT_MARGINS = 4;

    /**
     * Value in the Quickplay configuration
     */
    Object element;
    /**
     * GuiOption annotation of this value, which contains it's name and such
     */
    GuiOption optionInfo;
    /**
     * The name of the field in the configuration, used for saving values back to the config
     */
    String configFieldName;

    /**
     * Constructor
     *
     * @param element This configuration element
     * @param optionInfo This configuration element's {@link GuiOption} annotation
     * @param configFieldName Name of the field this configuration element is coming from
     */
    public ConfigElement(Object element, GuiOption optionInfo, String configFieldName) {
        // Only doubles, booleans, colors, runnables, and enums are allowed at the moment.
        if(
                element instanceof Double ||
                element instanceof Boolean ||
                element instanceof QuickplayColor ||
                element instanceof Runnable ||
                element.getClass().isEnum()
            ) {
            this.element = element;
        } else {
            throw new IllegalArgumentException("element not of recognized type! Recognized types: Double, Boolean, Color, Runnable, Enum");
        }
        this.optionInfo = optionInfo;
        this.configFieldName = configFieldName;
    }
}
