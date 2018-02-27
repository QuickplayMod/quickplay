package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiString extends QuickplayGuiComponent {
    public boolean centered;

    public QuickplayGuiString(Object origin, int id, int x, int y, int width, int height, String displayString, boolean centered, boolean scrollable) {
        super(origin, id, x, y, width, height, displayString, scrollable);
        this.centered = centered;
    }

    @Override
    public void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity) {
        final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        if(centered)
            drawCenteredString(gui.mc.fontRendererObj, displayString, x, scrollAdjustedY, (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        else
            drawString(gui.mc.fontRendererObj, displayString, x, scrollAdjustedY, (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        GL11.glPopMatrix();
    }

    @Override
    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        return (mouseX > x - width / 2 && mouseX < (x + width / 2)) && (mouseY > y && mouseY < (y + height));
    }

    @Override
    public void mouseReleased(QuickplayGui gui1, int mouseX, int mouseY) {

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
