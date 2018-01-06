package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.config.ConfigElement;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiHeader extends QuickplayGuiComponent {
    public QuickplayGuiHeader(ConfigElement originElement, int id, int x, int y, int width, int height, String displayString) {
        super(originElement, id, x, y, width, height, displayString);
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, double opacity) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        drawCenteredString(mc.fontRendererObj, displayString, x, y, (Quickplay.INSTANCE.settings.primaryColor.getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        GL11.glPopMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void mouseReleased(Minecraft mc, int mouseX, int mouseY) {

    }
}
