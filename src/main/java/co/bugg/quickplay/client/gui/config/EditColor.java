package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import org.lwjgl.opengl.GL11;

// TODO
public class EditColor extends QuickplayGui {
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw background
         */

        drawDefaultBackground();

        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {

    }
}
