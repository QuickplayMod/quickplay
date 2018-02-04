package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiButton extends QuickplayGuiComponent {
    public static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

    public boolean enabled = true;
    protected ResourceLocation texture = buttonTextures;
    protected int textureX = -1;
    protected int textureY = -1;
    protected double scale = 1.0;

    public QuickplayGuiButton(Object origin, int id, int x, int y, int widthIn, int heightIn, String text) {
        this(origin, id, x, y, widthIn, heightIn, text, null, -1, -1, 1.0);
    }

    public QuickplayGuiButton(Object origin, int id, int x, int y, int widthIn, int heightIn, String text, ResourceLocation texture, int textureX, int textureY, double scale) {
        super(origin, id, x, y, widthIn, heightIn, text);
        // Adjust width & height according to scale
        this.width = (int) (widthIn * scale);
        this.height = (int) (heightIn * scale);

        if(texture != null) {
            this.texture = texture;
        }
        this.textureX = textureX;
        this.textureY = textureY;
        this.scale = scale;
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, double opacity) {

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1, 1, 1, (float) opacity);

        // Update whether user is currently hovering over button
        hovering = (mouseX >= x && mouseX < x + width) && (mouseY >= y && mouseY < y + height);
        // Get the default button texture depending on if mouse is hovering or is enabled
        int buttonTextureMultiplier = getDefaultButtonTexture(hovering);

        // If default button
        GL11.glScaled(scale, scale, scale);
        if(texture == buttonTextures || textureX < 0 || textureY < 0) {
            // Draw the different parts of the button
            drawTexturedModalRect((int) (x / scale), (int) (y  / scale), 0, 46 + buttonTextureMultiplier * 20, width / 2, height);
            drawTexturedModalRect((int) (x + width / 2 / scale), (int) (y / scale), 200 - width / 2, 46 + buttonTextureMultiplier * 20, width / 2, height);
        } else {
            drawTexturedModalRect((int) (x / scale), (int) (y / scale), textureX, textureY, (int) (width / scale), (int) (height / scale));
        }
        GL11.glScaled(1 / scale, 1 / scale, 1 / scale);

        drawDisplayString(mc);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }

    public void drawDisplayString(Minecraft mc) {
        if(displayString != null && displayString.length() > 0) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);

            drawCenteredString(mc.fontRendererObj, displayString, x + width / 2, y + (height - 8) / 2, getDefaultTextColor(opacity));

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    public int getDefaultTextColor(double opacity) {
        int color;
        if (!enabled) {
            color = 0xA0A0A0;
        } else if (hovering) {
            color = 0xFFFFA0;
        } else {
            color = 0xE0E0E0;
        }

        return color & 0xFFFFFF | (int) (opacity * 255) << 24;
    }

    public int getDefaultButtonTexture(boolean mouseOver) {
        int i = 1;

        if (!enabled) {
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
    public boolean mouseHovering(Minecraft mc, int mouseX, int mouseY) {
        return (mouseX > x && mouseX < (x + width)) && (mouseY > y && mouseY < (y + height));
    }

    /**
     * We can ignore mouse release events for normal buttons
     */
    @Override
    public void mouseReleased(Minecraft mc, int mouseX, int mouseY) {
    }

    @Override
    public boolean keyTyped(char keyTyped, int keyCode) {
        return false;
    }

    @Override
    public boolean mouseClicked(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
