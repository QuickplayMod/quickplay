package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Component that extends off of Quickplay GUI buttons to add a number slider
 */
public class QuickplayGuiSlider extends QuickplayGuiButton {
    /**
     * Percentage of the way down the slider, from the left, that the handle is currently at
     */
    private float sliderPercentage;
    /**
     * Whether the mouse is currently pressed down or not
     */
    private boolean isMouseDown;
    /**
     * Display name of this slider
     */
    private String name;
    /**
     * Minimum value of this slider
     */
    private final float min;
    /**
     * Maximum value of this slider
     */
    private final float max;
    /**
     * {@link GuiPageButtonList.GuiResponder} the slider uses to send off changes in values
     * Whenever the slider's value changes, a method in the {@link GuiPageButtonList.GuiResponder} is called.
     */
    private final GuiPageButtonList.GuiResponder responder;
    /**
     * {@link FormatHelper} for formatting the slider's display string, depending on the value
     */
    private FormatHelper formatHelper;

    /**
     * Constructor
     *
     * @param guiResponder GUI responder for this slider
     * @param origin       Origin of this slider
     * @param idIn         ID of this slider
     * @param x            X position of this slider if scrolling = 0
     * @param y            Y position of this slider if scrolling = 0
     * @param widthIn      Width of this slider
     * @param heightIn     Height of this slider
     * @param name         Display name of this slider
     * @param min          Minimum value of this slider
     * @param max          Maxiumum value of this slider
     * @param defaultValue Default value of this slider
     * @param formatter    Display name formatter depending on the slider's value
     * @param scrollable   Whether this slider is scrollable
     */
    public QuickplayGuiSlider(GuiPageButtonList.GuiResponder guiResponder, Object origin, int idIn, int x, int y, int widthIn, int heightIn, String name, float min, float max, float defaultValue, FormatHelper formatter, boolean scrollable) {
        super(origin, idIn, x, y, widthIn, heightIn, "", scrollable);
        this.name = name;
        this.min = min;
        this.max = max;
        this.sliderPercentage = (defaultValue - min) / (max - min);
        this.formatHelper = formatter;
        this.responder = guiResponder;
        this.displayString = getDisplayString();
    }

    /**
     * Get the current value of this slider
     *
     * @return The current value
     */
    public float getValue() {
        return min + (max - min) * sliderPercentage;
    }

    /**
     * Get the display string for this slider
     * This method tries to use a formatHelper if one exists, otherwise uses a default format
     *
     * @return The full display string, after formatting
     */
    private String getDisplayString() {
        return formatHelper == null ? name + ": " + getValue() : formatHelper.getText(id, name, getValue());
    }

    // Unused by sliders
    @Override
    public int getDefaultButtonTexture(boolean mouseOver) {
        return 0;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if (opacity > 0) {
            super.draw(gui, mouseX, mouseY, opacity);

            final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            if (isMouseDown) {
                // Calculate the new slider position
                calculateSliderPos(mouseX);
                // Update the display string
                displayString = getDisplayString();
                // Handle input change
                responder.onTick(id, getValue());
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, ((Number) opacity).floatValue());
            gui.mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.scale(scale, scale, scale);
            drawTexturedModalRect(x + (int) (sliderPercentage * (float) (width - 8)), scrollAdjustedY, 0, 66, 4, 20);
            drawTexturedModalRect(x + (int) (sliderPercentage * (float) (width - 8)) + 4, scrollAdjustedY, 196, 66, 4, 20);
            if (opacity > 0)
                drawDisplayString(gui, opacity, scrollAdjustedY);
            GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    /**
     * Calculate the percentage from the left of the slider that the handle is at
     *
     * @param mouseX The X position of the mouse
     */
    private void calculateSliderPos(int mouseX) {
        sliderPercentage = (float) (mouseX - (x + 4)) / (float) (width - 8);
        // Keep the slider percentage between 0 and 1
        sliderPercentage = sliderPercentage < 0.0f ? 0.0f : sliderPercentage > 1.0f ? 1.0f : sliderPercentage;
    }

    @Override
    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        if (super.mouseHovering(gui, mouseX, mouseY)) {
            sliderPercentage = (float) (mouseX - (x / scale + 4)) / (float) (width / scale - 8);

            if (sliderPercentage < 0.0F) {
                sliderPercentage = 0.0F;
            }

            if (sliderPercentage > 1.0F) {
                sliderPercentage = 1.0F;
            }

            displayString = getDisplayString();
            responder.onTick(id, getValue());
            isMouseDown = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(QuickplayGui gui, int mouseX, int mouseY) {
        isMouseDown = false;
    }

    @SideOnly(Side.CLIENT)
    public interface FormatHelper {
        String getText(int id, String name, float value);
    }
}
