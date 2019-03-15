package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.io.Serializable;

/**
 * Element that can be moved across the player's HUD
 * Moveable with {@link MoveableHudElementEditor}
 */
public abstract class MoveableHudElement extends Gui implements Serializable {

    /**
     * Width of the screen
     */
    public int screenWidth;
    /**
     * Height of the screen
     */
    public int screenHeight;
    /**
     * Opacity of this element
     */
    public double opacity = 1;
    /**
     * Minecraft's resolution calculator
     */
    public ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

    /**
     * Edit this element in the {@link MoveableHudElementEditor}
     */
    public void edit() {
        Minecraft.getMinecraft().displayGuiScreen(new MoveableHudElementEditor(this));
    }

    /**
     * Render this element at the default location with full opacity
     */
    public void render() {
        this.render(getxRatio(), getyRatio(), 1);
    }

    /**
     * Render this element
     *
     * @param x       Percentage of the screen's width to render at
     * @param y       Percentage of the screen's height to render at
     * @param opacity Opacity to render at
     */
    public void render(double x, double y, double opacity) {
        this.opacity = opacity;
        scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        screenWidth = scaledResolution.getScaledWidth();
        screenHeight = scaledResolution.getScaledHeight();
    }

    /**
     * Set the percentage away from the left of the screen to render this at
     *
     * @param ratio Ratio, between 0 and 1
     */
    public abstract void setxRatio(double ratio);

    /**
     * Set the percentage away from the top of the screen to render this at
     *
     * @param ratio Ratio, between 0 and 1
     */
    public abstract void setyRatio(double ratio);

    /**
     * Get the percentage away from the left of the screen to render this at
     *
     * @return Ratio
     */
    public abstract double getxRatio();

    /**
     * Get the percentage away from the top of the screen to render this at
     *
     * @return Ratio
     */
    public abstract double getyRatio();

    /**
     * Save this element's position
     */
    public abstract void save();

    /**
     * Display size enum for the element
     * <p>
     * Because rendering is handled differently for each element, this needs to be
     * handled by individual elements that extend off of this class.
     */
    public enum Size {
        SMALL("quickplay.moveableHudElement.small", 0.5),
        MEDIUM("quickplay.moveableHudElement.medium", 1.0),
        LARGE("quickplay.moveableHudElement.large", 1.5),
        XLARGE("quickplay.moveableHudElement.xlarge", 2.0);

        /**
         * Translation key when displaying this enum value as a string
         */
        private String translationKey;
        /**
         * Scale associated with this value
         */
        private double scale;

        /**
         * Constructor
         *
         * @param translationKey Translation key for displaying this enum value as a string
         * @param scale          Scale associated with this enum value
         */
        Size(String translationKey, double scale) {
            this.translationKey = translationKey;
            this.scale = scale;
        }

        /**
         * Get {@link #translationKey}
         *
         * @return {@link #translationKey}
         */
        public String toString() {
            return translationKey;
        }

        /**
         * Get {@link #scale}
         *
         * @return {@link #scale}
         */
        public double getScale() {
            return scale;
        }
    }
}
