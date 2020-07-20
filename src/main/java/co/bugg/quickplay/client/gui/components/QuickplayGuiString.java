package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.client.renderer.GlStateManager;

/**
 * A simple, static string that is built into a component
 */
public class QuickplayGuiString extends QuickplayGuiComponent {
    /**
     * Whether the string should be centered
     */
    public boolean centered;

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
        this.centered = centered;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if(opacity > 0) {
            final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            if (centered) {
                drawCenteredString(gui.mc.fontRendererObj, displayString, x, scrollAdjustedY,
                        (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            } else {
                drawString(gui.mc.fontRendererObj, displayString, x, scrollAdjustedY,
                        (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            }
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        return (mouseX > x - width / 2 && mouseX < (x + width / 2)) && (mouseY > y && mouseY < (y + height));
    }

    @Override
    public void mouseReleased(QuickplayGui gui1, int mouseX, int mouseY) {

    }

    @Override
    public boolean keyTyped(char keyTyped, int keyCode) {
        return false;
    }

    @Override
    public boolean mouseClicked(QuickplayGui gui, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
