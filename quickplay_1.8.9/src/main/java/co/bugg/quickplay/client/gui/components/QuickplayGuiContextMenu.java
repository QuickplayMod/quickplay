package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.ContextMenu;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.ListIterator;

/**
 * Context menu for Quickplay GUIs whenever the user right-clicks on something that has context menu options
 */
public abstract class QuickplayGuiContextMenu extends QuickplayGuiComponent implements ContextMenu {

    /**
     * Opacity of the background box behind the menu
     */
    public final double boxOpacity = 0.7;
    /**
     * Scale of the menu
     */
    public final double scale = 1.0;
    /**
     * Padding between the text and the edges of the background box
     */
    public final int boxPadding = 3;
    /**
     * Margin on the bottom of strings (between each string)
     */
    public final int stringBottomMargin = 3;
    /**
     * The index of the currently highlighted option
     * -1 for none
     */
    public int highlightedOptionIndex = -1;

    /**
     * Constructor
     * Context menus cannot be scrolled
     * @param options A list of all options available to the user
     * @param origin The origin of this context menu
     * @param id The ID of this context menu
     * @param x The X position of this context menu
     * @param y The Y position of this context menu
     */
    public QuickplayGuiContextMenu(List<String> options, QuickplayGuiComponent origin, int id, int x, int y) {
        // Width / height calculated later
        super(origin, id, x, y, 0, 0, "", false);

        Quickplay.INSTANCE.registerEventHandler(this);

        // Calculate width
        int longestStringLength = 0;
        for(final String option : options) {
            if(this.getStringWidth(option) > longestStringLength) {
                longestStringLength = this.getStringWidth(option);
            }
        }

        this.width = longestStringLength + this.boxPadding * 2;
        this.height = options.size() * (getFontHeight() + this.stringBottomMargin) + this.boxPadding * 2;

        this.options.clear();
        this.options.addAll(options);
    }

    @Override
    public boolean hookKeyTyped(char keyTyped, int keyCode) {
        // Down/tab buttons move highlighting down the list. Up arrow/shift+tab does opposite. Enter key selects the option.
        if(keyCode == Keyboard.KEY_UP || (keyCode == Keyboard.KEY_TAB && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ||
                Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)))) {
            if(this.highlightedOptionIndex <= 0 || this.highlightedOptionIndex > QuickplayGuiContextMenu.options.size() - 1) {
                this.highlightedOptionIndex = QuickplayGuiContextMenu.options.size() - 1;
            } else {
                this.highlightedOptionIndex--;
            }
        } else if(keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_TAB) {
            if(this.highlightedOptionIndex < 0 || this.highlightedOptionIndex >= QuickplayGuiContextMenu.options.size() - 1) {
                this.highlightedOptionIndex = 0;
            } else {
                this.highlightedOptionIndex++;
            }
        } else if(keyCode == Keyboard.KEY_RETURN && this.highlightedOptionIndex >= 0) {
            this.optionSelected(this.highlightedOptionIndex);
            this.highlightedOptionIndex = -1;
        }
        return true;
    }

    @Override
    public boolean hookMouseClicked(QuickplayGui gui, int mouseX, int mouseY, int mouseButton) {
        if(this.isMouseHovering(gui, mouseX, mouseY)) {
            final int hoveringOver = this.mouseHoveringOverOption(gui, mouseX, mouseY);
            if(hoveringOver >= 0) {
                this.optionSelected(hoveringOver);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the index of the option the mouse is currently hovering over, if any
     * @param gui GUI this component is being rendered on
     * @param mouseX X position of the mouse
     * @param mouseY Y position of the mouse
     * @return The index of the option the mouse is currently hovering over, or -1 if none
     */
    private int mouseHoveringOverOption(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : this.y;

        for(ListIterator<String> iter = QuickplayGuiContextMenu.options.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final int stringY = (int) (scrollAdjustedY + this.boxPadding * this.scale + index *
                    (this.getFontHeight() + this.stringBottomMargin) * this.scale);

            if(mouseX > this.x && mouseX < this.x + this.width * this.scale && mouseY > stringY && mouseY <
                    stringY + this.getFontHeight() * this.scale) {
                return index;
            }

            iter.next();
        }
        return -1;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if(opacity > 0) {
            final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : this.y;

            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();

            GlStateManagerWrapper.scale(this.scale);

            // Draw right click box
            QuickplayGui.drawRect((int) (this.x / this.scale), (int) (scrollAdjustedY / this.scale), (int) (this.x / this.scale + this.width),
                    (int) (scrollAdjustedY / this.scale + this.height), (int) (opacity * this.boxOpacity * 255) << 24);
            GlStateManagerWrapper.enableBlend();

            for (ListIterator<String> iter = this.options.listIterator(); iter.hasNext(); ) {
                final int index = iter.nextIndex();
                final String string = iter.next();
                final int stringY = (int) (scrollAdjustedY / this.scale + this.boxPadding + index *
                        (this.getFontHeight() + this.stringBottomMargin));

                int color;
                if(this.highlightedOptionIndex == index) {
                    color = Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB();
                } else {
                    color = Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB();
                }

                this.drawString(string, (int) (this.x / this.scale + this.boxPadding), stringY,
                        color & 0xFFFFFF | (int) (opacity * 255) << 24);
                if (mouseX > this.x && mouseX < this.x + this.width * this.scale && mouseY > stringY * this.scale && mouseY <
                        (stringY + this.getFontHeight()) * this.scale) {

                    drawRect((int) (this.x / this.scale + this.boxPadding), stringY + this.getFontHeight(),
                            (int) (this.x / this.scale + this.boxPadding + this.getStringWidth(string)),
                            stringY + this.getFontHeight() + 1,
                            color & 0xFFFFFF | (int) (opacity * 255) << 24);
                }
            }

            GlStateManagerWrapper.scale(1/scale);

            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }



    @Override
    public boolean isMouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = this.scrollable ? this.y - gui.scrollPixel : y;
        return mouseX > this.x && mouseX < this.x + this.width * this.scale && mouseY > scrollAdjustedY &&
                mouseY < scrollAdjustedY + this.height * this.scale;
    }

    @Override
    public void hookMouseReleased(QuickplayGui gui, int mouseX, int mouseY) {

    }
}
