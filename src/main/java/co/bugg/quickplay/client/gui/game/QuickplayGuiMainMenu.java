package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.client.gui.QuickplayGui;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiMainMenu extends QuickplayGui {

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

    }
}
