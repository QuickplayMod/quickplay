package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

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
    public boolean isMouseDown;
    /**
     * Display name of this slider
     */
    private final String name;
    /**
     * Minimum value of this slider
     */
    private final float min;
    /**
     * Maximum value of this slider
     */
    private final float max;
    /**
     * {@link net.minecraft.client.gui.GuiPageButtonList.GuiResponder} the slider uses to send off changes in values
     * Whenever the slider's value changes, a method in the {@link net.minecraft.client.gui.GuiPageButtonList.GuiResponder} is called.
     */
    private final GuiPageButtonList.GuiResponder responder;
    /**
     * {@link FormatHelper} for formatting the slider's display string, depending on the value
     */
    private final QuickplayGuiSlider.FormatHelper formatHelper;

    /**
     * Constructor
     *
     * @param guiResponder GUI responder for this slider
     * @param origin Origin of this slider
     * @param idIn ID of this slider
     * @param x X position of this slider if scrolling = 0
     * @param y Y position of this slider if scrolling = 0
     * @param widthIn Width of this slider
     * @param heightIn Height of this slider
     * @param name Display name of this slider
     * @param min Minimum value of this slider
     * @param max Maxiumum value of this slider
     * @param defaultValue Default value of this slider
     * @param formatter Display name formatter depending on the slider's value
     * @param scrollable Whether this slider is scrollable
     */
    public QuickplayGuiSlider(GuiPageButtonList.GuiResponder guiResponder, Object origin, int idIn, int x, int y,
                              int widthIn, int heightIn, String name, float min, float max, float defaultValue,
                              QuickplayGuiSlider.FormatHelper formatter, boolean scrollable)
    {
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
     * @return The current value
     */
    public float getValue()
    {
        return this.min + (this.max - this.min) * this.sliderPercentage;
    }

    /**
     * Get the display string for this slider
     * This method tries to use a formatHelper if one exists, otherwise uses a default format
     * @return The full display string, after formatting
     */
    private String getDisplayString() {
        return this.formatHelper == null ? this.name + ": " + getValue() : this.formatHelper.getText(this.id, this.name, getValue());
    }

    // Unused by sliders
    @Override
    public int getDefaultButtonTexture(boolean mouseOver)
    {
        return 0;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if(opacity > 0) {
            super.draw(gui, mouseX, mouseY, opacity);

            final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : this.y;

            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();

            if (this.isMouseDown) {
                // Calculate the new slider position
                this.calculateSliderPos(mouseX);
                // Update the display string
                this.displayString = this.getDisplayString();
                // Handle input change
                this.responder.onTick(this.id, this.getValue());
            }

            GlStateManagerWrapper.color(1.0F, 1.0F, 1.0F, ((Number) opacity).floatValue());
            Quickplay.INSTANCE.minecraft.bindTexture(QuickplayGuiSlider.buttonTextures);
            GlStateManagerWrapper.scale(this.scale);
            this.drawTexturedModalRect(this.x + (int) (this.sliderPercentage * (float) (this.width - 8)), scrollAdjustedY,
                    0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int) (this.sliderPercentage * (float) (this.width - 8)) + 4, scrollAdjustedY,
                    196, 66, 4, 20);
            if (opacity > 0) {
                this.drawDisplayString(gui, opacity, scrollAdjustedY);
            }
            GlStateManagerWrapper.scale(1 / scale);

            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }

    /**
     * Calculate the percentage from the left of the slider that the handle is at
     * @param mouseX The X position of the mouse
     */
    private void calculateSliderPos(int mouseX) {
        this.sliderPercentage = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
        // Keep the slider percentage between 0 and 1
        this.sliderPercentage = this.sliderPercentage < 0.0f ? 0.0f : Math.min(this.sliderPercentage, 1.0f);
    }

    @Override
    public boolean isMouseHovering(QuickplayGui gui, int mouseX, int mouseY)
    {
        if (Mouse.isButtonDown(0) && super.isMouseHovering(gui, mouseX, mouseY)) {
            this.sliderPercentage = (float)(mouseX - (this.x / this.scale + 4)) / (float)(this.width / this.scale - 8);
            // Slider percentage must be between 0 and 1
            this.sliderPercentage = Math.min(Math.max(this.sliderPercentage, 0.0f), 1.0f);

            this.displayString = getDisplayString();
            this.responder.onTick(this.id, getValue());
            this.isMouseDown = true;
            return true;
        } else {
            return super.isMouseHovering(gui, mouseX, mouseY);
        }
    }

    @Override
    public void hookMouseReleased(QuickplayGui gui, int mouseX, int mouseY)
    {
        this.isMouseDown = false;
    }

    @SideOnly(Side.CLIENT)
    public interface FormatHelper
    {
        String getText(int id, String name, float value);
    }
}
