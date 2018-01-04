package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class QuickplayGuiButton extends GuiButton {
    public double lastOpacity = 1;

    public QuickplayGuiButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public QuickplayGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, double opacity) {
        this.lastOpacity = opacity;
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.color(1, 1, 1, (float) opacity);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
            GlStateManager.enableBlend();
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            GlStateManager.enableBlend();
            this.mouseDragged(mc, mouseX, mouseY, opacity);
            int j = 14737632 & 0xFFFFFF | (int) (opacity * 255) << 24;

            if (packedFGColour != 0)
            {
                j = packedFGColour & 0xFFFFFF | (int) (opacity * 255) << 24;
            }
            else
            if (!this.enabled)
            {
                j = 10526880 & 0xFFFFFF | (int) (opacity * 255) << 24;
            }
            else if (this.hovered)
            {
                j = 16777120 & 0xFFFFFF | (int) (opacity * 255) << 24;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }

    // Things like GUI sliders need the opacity in this method in order to draw the slider handle correctly
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY, double opacity) {
    }

    public synchronized void move(int distance) {
        this.yPosition = yPosition + distance;
    }
}
