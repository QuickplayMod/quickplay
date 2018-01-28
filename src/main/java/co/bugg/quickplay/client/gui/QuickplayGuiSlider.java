package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiSlider extends QuickplayGuiButton {
    private float sliderPercentage = 1.0F;
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
        this.sliderPercentage = (defaultValue - min) / (max - min);
        this.formatHelper = formatter;
        this.responder = guiResponder;
        this.displayString = getDisplayString();
    }

    public float getValue()
    {
        return min + (max - min) * sliderPercentage;
    }

    private String getDisplayString()
    {
        return formatHelper == null ? name + ": " + getValue() : formatHelper.getText(id, name, getValue());
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    public int getDefaultButtonTexture(boolean mouseOver)
    {
        return 0;
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, double opacity) {
        super.draw(mc, mouseX, mouseY, opacity);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        if (isMouseDown) {
            // Calculate the new slider position
            calculateSliderPos(mouseX);
            // Update the display string
            displayString = getDisplayString();
            // Handle input change
            responder.onTick(id, getValue());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, ((Number) opacity).floatValue());
        mc.getTextureManager().bindTexture(buttonTextures);
        GL11.glScaled(scale, scale, scale);
        drawTexturedModalRect(x + (int)(sliderPercentage * (float)(width - 8)), y, 0, 66, 4, 20);
        drawTexturedModalRect(x + (int)(sliderPercentage * (float)(width - 8)) + 4, y, 196, 66, 4, 20);
        drawDisplayString(mc);
        GL11.glScaled(1 / scale, 1 / scale, 1 / scale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void calculateSliderPos(int mouseX) {
        sliderPercentage = (float)(mouseX - (x + 4)) / (float)(width - 8);
        // Keep the slider percentage between 0 and 1
        sliderPercentage = sliderPercentage < 0.0f ? 0.0f : sliderPercentage > 1.0f ? 1.0f : sliderPercentage;
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            sliderPercentage = (float)(mouseX - (x / scale + 4)) / (float)(width / scale - 8);

            if (sliderPercentage < 0.0F)
            {
                sliderPercentage = 0.0F;
            }

            if (sliderPercentage > 1.0F)
            {
                sliderPercentage = 1.0F;
            }

            displayString = getDisplayString();
            responder.onTick(id, getValue());
            isMouseDown = true;
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
        isMouseDown = false;
    }

    @SideOnly(Side.CLIENT)
    public interface FormatHelper
    {
        String getText(int id, String name, float value);
    }
}
