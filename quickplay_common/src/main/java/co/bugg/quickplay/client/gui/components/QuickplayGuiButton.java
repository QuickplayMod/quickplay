package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import co.bugg.quickplay.wrappers.ResourceLocationWrapper;

/**
 * Quickplay's equivalent to a Minecraft button
 */
public class QuickplayGuiButton extends QuickplayGuiComponent {
    /**
     * Vanilla texture location for Minecraft buttons
     */
    public static final ResourceLocationWrapper buttonTextures = new ResourceLocationWrapper("textures/gui/widgets.png");
    /**
     * Whether this button is enabled or not
     */
    public boolean enabled = true;
    /**
     * Texture location for this button in particular
     */
    protected ResourceLocationWrapper texture = QuickplayGuiButton.buttonTextures;
    /**
     * X location corresponding to the left of this button's texture
     * -1 means draw as a vanilla button
     */
    protected int textureX;
    /**
     * Y location corresponding to the top of this button's texture
     * -1 means draw as a vanilla button
     */
    protected int textureY;
    /**
     * Scale of this button
     */
    protected double scale;

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
    public QuickplayGuiButton(Object origin, int id, int x, int y, int widthIn, int heightIn, String text,
                              ResourceLocationWrapper texture, int textureX, int textureY, double scale, boolean scrollable) {
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
            final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : this.y;

            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();
            Quickplay.INSTANCE.minecraft.bindTexture(this.texture);
            GlStateManagerWrapper.color(1, 1, 1, (float) opacity);

            // Update whether user is currently hovering over button
            hovering = this.isMouseHovering(gui, mouseX, mouseY);
            // Get the default button texture depending on if mouse is hovering or is enabled
            int buttonTextureMultiplier = this.getDefaultButtonTexture(hovering);

            // If default button
            GlStateManagerWrapper.scale(scale);
            if (this.texture == QuickplayGuiButton.buttonTextures || this.textureX < 0 || this.textureY < 0) {
                // Draw the different parts of the button
                this.drawTexturedModalRect((int) (this.x / this.scale), (int) (scrollAdjustedY / this.scale), 0,
                        46 + buttonTextureMultiplier * 20, this.width / 2, this.height);
                this.drawTexturedModalRect((int) (this.x + this.width / 2 / this.scale), (int) (scrollAdjustedY / this.scale),
                        200 - this.width / 2, 46 + buttonTextureMultiplier * 20, this.width / 2, this.height);
            } else {
                this.drawTexturedModalRect((int) (this.x / this.scale), (int) (scrollAdjustedY / this.scale), this.textureX, this.textureY,
                        (int) (this.width / this.scale), (int) (this.height / this.scale));
            }
            GlStateManagerWrapper.scale(1/scale);

            if (opacity > 0) {
                this.drawDisplayString(gui, opacity, scrollAdjustedY);
            }

            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
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
            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();

            this.drawCenteredString(this.displayString, this.x + this.width / 2,
                    scrollAdjustedY + (this.height - 8) / 2, this.getDefaultTextColor(opacity));

            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }

    /**
     * Get the text color for the button's display text, depending on if the button is enabled or being hovered over
     * @param opacity Opacity of the text
     * @return Color code for the text
     */
    public int getDefaultTextColor(double opacity) {
        int color;
        if (!this.enabled) {
            color = 0xA0A0A0;
        } else if (this.hovering) {
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

        if (!this.enabled) {
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
    public void hookMouseReleased(QuickplayGui gui, int mouseX, int mouseY) {
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
