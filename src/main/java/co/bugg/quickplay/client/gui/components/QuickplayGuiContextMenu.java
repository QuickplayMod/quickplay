package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.ContextMenu;
import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.ListIterator;

import static org.lwjgl.input.Keyboard.*;

/**
 * Context menu for Quickplay GUIs whenever the user right-clicks on something that has context menu options
 */
public abstract class QuickplayGuiContextMenu extends QuickplayGuiComponent implements ContextMenu {

    /**
     * Opacity of the background box behind the menu
     */
    private static final double boxOpacity = 0.7;
    /**
     * Scale of the menu
     */
    public static final double scale = 1.0;
    /**
     * Padding between the text and the edges of the background box
     */
    private static final int boxPadding = 3;
    /**
     * Margin on the bottom of strings (between each string)
     */
    private static final int stringBottomMargin = 3;
    /**
     * The index of the currently highlighted option
     * -1 for none
     */
    private static int highlightedOptionIndex = -1;
    /**
     * The font renderer to use
     */
    private static final FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

    /**
     * Constructor
     * Context menus cannot be scrolled
     *
     * @param options A list of all options available to the user
     * @param origin  The origin of this context menu
     * @param id      The ID of this context menu
     * @param x       The X position of this context menu
     * @param y       The Y position of this context menu
     */
    protected QuickplayGuiContextMenu(List<String> options, QuickplayGuiComponent origin, int id, int x, int y) {
        // Width / height calculated later
        super(origin, id, x, y, 0, 0, "", false);

        Quickplay.INSTANCE.registerEventHandler(this);

        // Calculate width
        int longestStringLength = 0;
        for (String option : options) {
            if (fontRendererObj.getStringWidth(option) > longestStringLength)
                longestStringLength = fontRendererObj.getStringWidth(option);
        }

        this.width = longestStringLength + boxPadding * 2;
        this.height = options.size() * (fontRendererObj.FONT_HEIGHT + stringBottomMargin) + boxPadding * 2;

        this.options.clear();
        this.options.addAll(options);
    }

    @Override
    public boolean keyTyped(char keyTyped, int keyCode) {

        // Down/tab buttons move highlighting down the list. Up arrow/shift+tab does opposite. Enter key selects the option.
        if (keyCode == KEY_UP || (keyCode == KEY_TAB && (isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(KEY_RSHIFT)))) {
            if (highlightedOptionIndex <= 0 || highlightedOptionIndex > options.size() - 1)
                highlightedOptionIndex = options.size() - 1;
            else
                highlightedOptionIndex--;
        } else if (keyCode == KEY_DOWN || keyCode == KEY_TAB) {
            if (highlightedOptionIndex < 0 || highlightedOptionIndex >= options.size() - 1)
                highlightedOptionIndex = 0;
            else
                highlightedOptionIndex++;
        } else if (keyCode == KEY_RETURN && highlightedOptionIndex >= 0) {
            optionSelected(highlightedOptionIndex);
            highlightedOptionIndex = -1;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(QuickplayGui gui, int mouseX, int mouseY, int mouseButton) {
        if (mouseHovering(gui, mouseX, mouseY)) {
            final int hoveringOver = mouseHoveringOverOption(gui, mouseX, mouseY);
            if (hoveringOver >= 0) {
                optionSelected(hoveringOver);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the index of the option the mouse is currently hovering over, if any
     *
     * @param gui    GUI this component is being rendered on
     * @param mouseX X position of the mouse
     * @param mouseY Y position of the mouse
     * @return The index of the option the mouse is currently hovering over, or -1 if none
     */
    private int mouseHoveringOverOption(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

        for (ListIterator<String> iter = options.listIterator(); iter.hasNext(); ) {
            final int index = iter.nextIndex();
            final int stringY = (int) (scrollAdjustedY + boxPadding * scale + index * (fontRendererObj.FONT_HEIGHT + stringBottomMargin) * scale);
            if (mouseX > x && mouseX < x + width * scale && mouseY > stringY && mouseY < stringY + fontRendererObj.FONT_HEIGHT * scale)
                return index;

            iter.next();
        }
        return -1;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        if (opacity > 0) {
            final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.scale(scale, scale, scale);

            // Draw right click box
            drawRect((int) (x / scale), (int) (scrollAdjustedY / scale), (int) (x / scale + width), (int) (scrollAdjustedY / scale + height), (int) (opacity * boxOpacity * 255) << 24);
            GlStateManager.enableBlend();

            for (ListIterator<String> iter = options.listIterator(); iter.hasNext(); ) {
                final int index = iter.nextIndex();
                final String string = iter.next();
                final int stringY = (int) (scrollAdjustedY / scale + boxPadding + index * (fontRendererObj.FONT_HEIGHT + stringBottomMargin));
                final int color = highlightedOptionIndex == index ? Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() : Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB();
                drawString(fontRendererObj, string, (int) (x / scale + boxPadding), stringY, color & 0xFFFFFF | (int) (opacity * 255) << 24);
                if (mouseX > x && mouseX < x + width * scale && mouseY > stringY * scale && mouseY < (stringY + fontRendererObj.FONT_HEIGHT) * scale)
                    drawRect((int) (x / scale + boxPadding), stringY + fontRendererObj.FONT_HEIGHT, (int) (x / scale + boxPadding + fontRendererObj.getStringWidth(string)), stringY + fontRendererObj.FONT_HEIGHT + 1, color & 0xFFFFFF | (int) (opacity * 255) << 24);
            }

            GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }


    @Override
    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;

        return mouseX > x && mouseX < x + width * scale && mouseY > scrollAdjustedY && mouseY < scrollAdjustedY + height * scale;
    }

    @Override
    public void mouseReleased(QuickplayGui gui, int mouseX, int mouseY) {

    }
}
