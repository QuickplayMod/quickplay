package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.ContextMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.ListIterator;

import static org.lwjgl.input.Keyboard.*;

public abstract class QuickplayGuiContextMenu extends QuickplayGuiComponent implements ContextMenu {

    public final double boxOpacity = 0.7;
    public final double scale = 1.0;
    public final int boxPadding = 3;
    public final int stringBottomMargin = 3;

    public int highlightedOptionIndex = -1;

    final FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

    public QuickplayGuiContextMenu(List<String> options, QuickplayGuiComponent origin, int id, int x, int y) {
        // Width / height calculated later
        super(origin, id, x, y, 0, 0, "");

        Quickplay.INSTANCE.registerEventHandler(this);

        // Calculate width
        int longestStringLength = 0;
        for(String option : options) {
            if(fontRendererObj.getStringWidth(option) > longestStringLength)
                longestStringLength = fontRendererObj.getStringWidth(option);
        }

        this.width = longestStringLength + boxPadding * 2;
        this.height = options.size() * (fontRendererObj.FONT_HEIGHT + stringBottomMargin) + boxPadding * 2;

        this.options.clear();
        this.options.addAll(options);
    }

    @Override
    public boolean keyTyped(char keyTyped, int keyCode) {
        System.out.println("Key pressed");

        // Down/tab buttons move highlighting down the list. Up arrow/shift+tab does opposite. Enter key selects the option.
        if(keyCode == KEY_UP || (keyCode == KEY_TAB && (isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(KEY_RSHIFT)))) {
            if(highlightedOptionIndex <= 0 || highlightedOptionIndex > options.size() - 1)
                highlightedOptionIndex = options.size() - 1;
            else
                highlightedOptionIndex--;
        } else if(keyCode == KEY_DOWN || keyCode == KEY_TAB) {
            if(highlightedOptionIndex < 0 || highlightedOptionIndex >= options.size() - 1)
                highlightedOptionIndex = 0;
            else
                highlightedOptionIndex++;
        } else if(keyCode == KEY_RETURN && highlightedOptionIndex >= 0) {
            optionSelected(highlightedOptionIndex);
            highlightedOptionIndex = -1;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        if(mouseHovering(mc, mouseX, mouseY)) {
            final int hoveringOver = mouseHoveringOverOption(mouseX, mouseY);
            if(hoveringOver >= 0) {
                optionSelected(hoveringOver);
                return true;
            }
        }
        return false;
    }

    private int mouseHoveringOverOption(int mouseX, int mouseY) {
        for(ListIterator<String> iter = options.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final int stringY = (int) (y + boxPadding * scale + index * (fontRendererObj.FONT_HEIGHT + stringBottomMargin) * scale);
            if(mouseX > x && mouseX < x + width * scale && mouseY > stringY && mouseY < stringY + fontRendererObj.FONT_HEIGHT * scale)
                return index;

            iter.next();
        }
        return -1;
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, double opacity) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glScaled(scale, scale, scale);

        // Draw right click box
        drawRect((int) (x / scale), (int) (y / scale), (int) (x / scale + width), (int) (y / scale + height), (int) (opacity * boxOpacity * 255) << 24);
        GL11.glEnable(GL11.GL_BLEND);

        for(ListIterator<String> iter = options.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final String string = iter.next();
            final int stringY = (int) (y / scale + boxPadding + index * (fontRendererObj.FONT_HEIGHT + stringBottomMargin));
            final int color = highlightedOptionIndex == index ? Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() : Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB();
            drawString(fontRendererObj, string, (int) (x / scale + boxPadding), stringY, color & 0xFFFFFF | (int) (opacity * 255) << 24);
            if(mouseX > x && mouseX < x + width * scale && mouseY > stringY * scale && mouseY < (stringY + fontRendererObj.FONT_HEIGHT) * scale)
                drawRect((int) (x / scale + boxPadding), stringY + fontRendererObj.FONT_HEIGHT, (int) (x / scale + boxPadding + fontRendererObj.getStringWidth(string)), stringY + fontRendererObj.FONT_HEIGHT + 1, color & 0xFFFFFF | (int) (opacity * 255) << 24);
        }

        GL11.glScaled(1 / scale, 1 / scale, 1 / scale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }



    @Override
    public boolean mouseHovering(Minecraft mc, int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width * scale && mouseY > y && mouseY < y + height * scale;
    }

    @Override
    public void mouseReleased(Minecraft mc, int mouseX, int mouseY) {

    }
}
