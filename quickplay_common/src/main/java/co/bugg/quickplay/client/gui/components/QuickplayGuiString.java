package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;

/**
 * A simple, static string that is built into a component
 */
public class QuickplayGuiString extends QuickplayGuiComponent {
    /**
     * Whether the string should be centered
     */
    public boolean centered;
    /**
     * Whether to render the string in the user's secondary color.
     */
    public boolean secondaryColor;
    public float scale;

    /**
     * Constructor
     *
     * @param origin Origin of this component
     * @param id ID of this component
     * @param x X position of this component when scrolling = 0
     * @param y Y position of this component when scrolling = 0
     * @param width Width of this component - Doesn't do much in this case unless you want to detect clicks on this component
     * @param height Height of this component - Doesn't do much in this case unless you want to detect clicks on this component
     * @param displayString String to be drawn
     * @param centered Whether this string should be centered
     * @param scrollable Whether this string is scrollable
     */
    public QuickplayGuiString(Object origin, int id, int x, int y, int width, int height, String displayString,
                              boolean centered, boolean scrollable) {
        super(origin, id, x, y, width, height, displayString, scrollable);
        this.secondaryColor = false;
        this.centered = centered;
        this.scale = 1.0f;
    }
    /**
     * Constructor
     *
     * @param origin Origin of this component
     * @param id ID of this component
     * @param x X position of this component when scrolling = 0
     * @param y Y position of this component when scrolling = 0
     * @param width Width of this component - Doesn't do much in this case unless you want to detect clicks on this component
     * @param height Height of this component - Doesn't do much in this case unless you want to detect clicks on this component
     * @param displayString String to be drawn
     * @param centered Whether this string should be centered
     * @param scrollable Whether this string is scrollable
     * @param secondaryColor Whether this string should be rendered in the secondaryColor
     */
    public QuickplayGuiString(Object origin, int id, int x, int y, int width, int height, String displayString,
                              boolean centered, boolean scrollable, boolean secondaryColor) {
        this(origin, id, x, y, width, height, displayString, centered, scrollable);
        this.secondaryColor = secondaryColor;
    }

    public QuickplayGuiString(Object origin, int id, int x, int y, int width, int height, String displayString,
                              boolean centered, boolean scrollable, boolean secondaryColor, float scale) {
        this(origin, id, x, y, width, height, displayString, centered, scrollable, secondaryColor);
        this.scale = scale;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if(opacity > 0) {
            final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : y;
            final QuickplayColor color = this.secondaryColor ?
                    Quickplay.INSTANCE.settings.secondaryColor : Quickplay.INSTANCE.settings.primaryColor;

            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();
            GlStateManagerWrapper.scale(this.scale);
            if (this.centered) {
                this.drawCenteredString(this.displayString, (int) (this.x / this.scale), (int) (scrollAdjustedY / this.scale),
                        (color.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            } else {
                this.drawString(this.displayString, (int) (this.x / this.scale), (int) (scrollAdjustedY / this.scale),
                        (color.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            }
            GlStateManagerWrapper.scale(1/this.scale);
            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }

    @Override
    public boolean isMouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        return (mouseX > this.x - this.width / 2 && mouseX < (this.x + this.width / 2)) &&
                (mouseY > this.y && mouseY < (this.y + this.height));
    }

    @Override
    public void hookMouseReleased(QuickplayGui gui1, int mouseX, int mouseY) {

    }

    @Override
    public boolean hookKeyTyped(char keyTyped, int keyCode) {
        return false;
    }

    @Override
    public boolean hookMouseClicked(QuickplayGui gui, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
