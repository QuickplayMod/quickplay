package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuickplayGuiSlider extends QuickplayGuiButton {
    private float sliderPosition = 1.0F;
    public boolean isMouseDown;
    private String name;
    private final float min;
    private final float max;
    private final GuiPageButtonList.GuiResponder responder;
    private QuickplayGuiSlider.FormatHelper formatHelper;

    public QuickplayGuiSlider(GuiPageButtonList.GuiResponder guiResponder, Object origin, int idIn, int x, int y, int widthIn, int heightIn, String name, float min, float max, float defaultValue, QuickplayGuiSlider.FormatHelper formatter)
    {
        super(origin, idIn, x, y, widthIn, heightIn, "");
        this.name = name;
        this.min = min;
        this.max = max;
        this.sliderPosition = (defaultValue - min) / (max - min);
        this.formatHelper = formatter;
        this.responder = guiResponder;
        this.displayString = this.getDisplayString();
    }

    public float getValue()
    {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    private String getDisplayString()
    {
        return this.formatHelper == null ? I18n.format(this.name) + ": " + this.getValue() : this.formatHelper.getText(this.id, I18n.format(this.name), this.getValue());
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    public int getButtonTexture(boolean mouseOver)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY, double opacity)
    {
        if (this.isMouseDown)
        {
            this.sliderPosition = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);

            if (this.sliderPosition < 0.0F)
            {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F)
            {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            this.responder.onTick(this.id, this.getValue());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, ((Number) opacity).floatValue());
        this.drawTexturedModalRect(this.x + (int)(this.sliderPosition * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
        this.drawTexturedModalRect(this.x + (int)(this.sliderPosition * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);

    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            this.sliderPosition = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);

            if (this.sliderPosition < 0.0F)
            {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F)
            {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            this.responder.onTick(this.id, this.getValue());
            this.isMouseDown = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    @Override
    public void mouseReleased(Minecraft mc, int mouseX, int mouseY)
    {
        this.isMouseDown = false;
    }

    @SideOnly(Side.CLIENT)
    public interface FormatHelper
    {
        String getText(int id, String name, float value);
    }
}
