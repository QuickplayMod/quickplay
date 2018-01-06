package co.bugg.quickplay.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class MoveableHudElementEditor extends QuickplayGui {

    private final MoveableHudElement element;

    double xRatio;
    double yRatio;

    public MoveableHudElementEditor(MoveableHudElement element) {
        this.element = element;
        this.xRatio = element.getxRatio();
        this.yRatio = element.getyRatio();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Blend is enabled for the GUI fadein
        // Fade in opacity has to be applied individually to each component that you want to fade in
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw background
         */

        // Prepend opacity to 24-bit color
        drawRect(0, 0, width, height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));
        // drawRect disables blend (Grr!)
        GL11.glEnable(GL11.GL_BLEND);

        element.render(xRatio, yRatio, opacity);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        moveTo(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        moveTo(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        save();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        // Move right 1 pixel
        if(keyCode == 205) {
            xRatio = ((xRatio * (double) element.screenWidth) + 1) / (double) element.screenWidth;
        // Move left 1 pixel
        } else if(keyCode == 203) {
            xRatio = ((xRatio * (double) element.screenWidth) - 1) / (double) element.screenWidth;
        // Move up 1 pixel
        } else if(keyCode == 200) {
            yRatio = ((yRatio * (double) element.screenHeight) - 1) / (double) element.screenHeight;
        // Move down 1 pixel
        } else if(keyCode == 208) {
            yRatio = ((yRatio * (double) element.screenHeight) + 1) / (double) element.screenHeight;
        }

        save();
    }



    public void save() {
        element.setxRatio(xRatio);
        element.setyRatio(yRatio);
        element.save();
    }

    public void moveTo(int mouseX, int mouseY) {
        xRatio = (double) mouseX / (double) element.screenWidth;
        yRatio = (double) mouseY / (double) element.screenHeight;
    }
}
