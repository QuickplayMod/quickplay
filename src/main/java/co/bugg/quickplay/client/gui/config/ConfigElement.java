package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.config.GuiOption;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class ConfigElement {
    public static final int ELEMENT_HEIGHT = 20;
    public static final int ELEMENT_MARGINS = 4;

    Object element;
    GuiOption optionInfo;

    public ConfigElement(Object element, GuiOption optionInfo) {
        if(
                element instanceof String ||
                element instanceof Integer ||
                element instanceof Double ||
                element instanceof Boolean ||
                element instanceof Color ||
                element instanceof Runnable
            ) {
            this.element = element;
        } else {
            throw new IllegalArgumentException("element not of recognized type! Recognized types: String, Integer, Double, Boolean, Color, Runnable");
        }
        this.optionInfo = optionInfo;
    }
}
