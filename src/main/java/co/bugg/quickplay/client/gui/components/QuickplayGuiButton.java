package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Quickplay's equivalent to a Minecraft button
 */
public class QuickplayGuiButton extends QuickplayGuiComponent {
    /**
     * Vanilla texture location for Minecraft buttons
     */
    public static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    /**
     * Whether this button is enabled or not
     */
    public boolean enabled = true;
    /**
     * Texture location for this button in particular
     */
    protected ResourceLocation texture = buttonTextures;
    /**
     * X location corresponding to the left of this button's texture
     * -1 means draw as a vanilla button
     */
    protected int textureX = -1;
    /**
     * Y location corresponding to the top of this button's texture
     * -1 means draw as a vanilla button
     */
    protected int textureY = -1;
    /**
     * Scale of this button
     */
    protected double scale = 1.0;

    /**
     * Constructor
     *
     * @param origin The origin of this button
     * @param id The ID of this button
     * @param x The X position of this button when scrolling = 0
     * @param y The Y position of this button when scrolling = 0
     * @param widthIn The width of this button
     * @param heightIn The height of this button
     * @param text The text that should be rendered on top of this button
     * @param scrollable Whether this button should be scrollable
     */
    public QuickplayGuiButton(Object origin, int id, int x, int y, int widthIn, int heightIn, String text, boolean scrollable) {
        this(origin, id, x, y, widthIn, heightIn, text, null, -1, -1, 1.0, scrollable);
    }

    /**
     * Constructor
     *
     * @param origin The origin of this button
     * @param id The ID of this button
     * @param x The X position of this button when scrolling = 0
     * @param y The Y position of this button when scrolling = 0
     * @param widthIn The width of this button
     * @param heightIn The height of this button
     * @param text The text to display on top of this button
     * @param texture The resource location to the texture to render as this button
     * @param textureX The X location within the texture that corresponds to the left of this button
     * @param textureY The Y location within the texture that corresponds to the top of this button
     * @param scale The scale of this button
     * @param scrollable Whether this button should be scrollable
     */
    public QuickplayGuiButton(Object origin, int id, int x, int y, int widthIn, int heightIn, String text, ResourceLocation texture, int textureX, int textureY, double scale, boolean scrollable) {
        super(origin, id, x, y, widthIn, heightIn, text, scrollable);
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
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if(opacity > 0) {
            final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            gui.mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1, 1, 1, (float) opacity);

            // Update whether user is currently hovering over button
            hovering = (mouseX >= x && mouseX < x + width) && (mouseY >= scrollAdjustedY && mouseY < scrollAdjustedY + height);
            // Get the default button texture depending on if mouse is hovering or is enabled
            int buttonTextureMultiplier = getDefaultButtonTexture(hovering);

            // If default button
            GL11.glScaled(scale, scale, scale);
            if (texture == buttonTextures || textureX < 0 || textureY < 0) {
                // Draw the different parts of the button
                drawTexturedModalRect((int) (x / scale), (int) (scrollAdjustedY / scale), 0, 46 + buttonTextureMultiplier * 20, width / 2, height);
                drawTexturedModalRect((int) (x + width / 2 / scale), (int) (scrollAdjustedY / scale), 200 - width / 2, 46 + buttonTextureMultiplier * 20, width / 2, height);
            } else {
                drawTexturedModalRect((int) (x / scale), (int) (scrollAdjustedY / scale), textureX, textureY, (int) (width / scale), (int) (height / scale));
            }
            GL11.glScaled(1 / scale, 1 / scale, 1 / scale);

            if (opacity > 0)
                drawDisplayString(gui, opacity, scrollAdjustedY);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    /**
     * Draw the string in the center of the button
     *
     * @param gui GUI this is being drawn on
     * @param opacity Opacity of the button/text
     * @param scrollAdjustedY The Y position on the GUI this string should be drawn at, adjusted for scrolling
     */
    public void drawDisplayString(QuickplayGui gui, double opacity, int scrollAdjustedY) {
        if(displayString != null && displayString.length() > 0) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);

            drawCenteredString(gui.mc.fontRendererObj, displayString, x + width / 2, scrollAdjustedY + (height - 8) / 2, getDefaultTextColor(opacity));

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    /**
     * Get the text color for the button's display text, depending on if the button is enabled or being hovered over
     * @param opacity Opacity of the text
     * @return Color code for the text
     */
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

    /**
     * Get the multiplier used by vanilla Minecraft to determine which texture to draw
     * @param mouseOver Whether the mouse is currently over the button
     * @return multiplier for which position the texture to be used is in
     */
    public int getDefaultButtonTexture(boolean mouseOver) {
        int i = 1;

        if (!enabled) {
            i = 0;
        } else if (mouseOver) {
            i = 2;
        }

        return i;
    }

    /**
     * Called whenever the mouse is dragged
     * @param gui GUI this button is being drawn on
     * @param mouseX X position of the mouse
     * @param mouseY Y position of the mouse
     * @param opacity Opacity of the screen
     */
    // Things like GUI sliders need the opacity in this method in order to draw the slider handle correctly
    protected void mouseDragged(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
    }

    // We can ignore mouse release events for normal buttons
    @Override
    public void mouseReleased(QuickplayGui gui, int mouseX, int mouseY) {
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
