package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.client.gui.config.ConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class QuickplayGuiButton extends QuickplayGuiComponent {
    public static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

    public double lastOpacity = 1;
    public boolean enabled = true;

    public QuickplayGuiButton(ConfigElement originElement, int id, int x, int y, int widthIn, int heightIn, String text) {
        super(originElement, id, x, y, widthIn, heightIn, text);
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, double opacity) {
        this.lastOpacity = opacity;

        FontRenderer fontrenderer = mc.fontRendererObj;
        mc.getTextureManager().bindTexture(buttonTextures);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovering = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int i = this.getButtonTexture(this.hovering);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1, 1, 1, (float) opacity);
        this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        GlStateManager.enableBlend();
        this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        GlStateManager.enableBlend();
        this.mouseDragged(mc, mouseX, mouseY, opacity);
        int j = 14737632 & 0xFFFFFF | (int) (opacity * 255) << 24;

        if (!this.enabled)
        {
            j = 10526880 & 0xFFFFFF | (int) (opacity * 255) << 24;
        }
        else if (this.hovering)
        {
            j = 16777120 & 0xFFFFFF | (int) (opacity * 255) << 24;
        }

        this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);

    }

    public int getButtonTexture(boolean mouseOver) {
        int i = 1;

        if (!this.enabled) {
            i = 0;
        } else if (mouseOver) {
            i = 2;
        }

        return i;
    }

    // Things like GUI sliders need the opacity in this method in order to draw the slider handle correctly
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY, double opacity) {
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height);
    }

    /**
     * We can ignore mouse release events for normal buttons
     */
    @Override
    public void mouseReleased(Minecraft mc, int mouseX, int mouseY) {
    }
}
