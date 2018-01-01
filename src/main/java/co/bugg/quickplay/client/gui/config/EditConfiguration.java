package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.config.AConfiguration;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class EditConfiguration extends QuickplayGui {
    public AConfiguration config;

    public EditConfiguration(AConfiguration config) {
        this.config = config;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Blend is enabled for the GUI fadein
        // Fade in opacity has to be applied individually to each component that you want to fade in
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawRect(0, 0, width, height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));
        // drawRect disables blend (Grr!)
        GL11.glEnable(GL11.GL_BLEND);

        double headerScale = height > 400 ? 2 : 1.5;
        double subheaderScale = height > 400 ? 1.3 : 1;
        GL11.glScaled(headerScale, headerScale, headerScale);
        drawCenteredString(fontRendererObj, "Quickplay Config", (int) (width / 2 / headerScale), (int) (height * 0.05 / headerScale), 0xFF3333 | ((int) (opacity * 255) << 24));
        GL11.glScaled( 1 / headerScale, 1 / headerScale, 1 / headerScale);

        GL11.glScaled(subheaderScale, subheaderScale, subheaderScale);
        drawCenteredString(fontRendererObj, "Version 2.0", (int) (width / 2 / subheaderScale), (int) (height * 0.05 / subheaderScale) + (int) (fontRendererObj.FONT_HEIGHT * headerScale) + 3, 0xCCFF99 | ((int) (opacity * 255) << 24));
        GL11.glScaled(1 / subheaderScale, 1 / subheaderScale, 1 / subheaderScale);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, 20, 20, "Testing!"));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        System.out.println("Key typed");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        System.out.println("Action performed");
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        System.out.println("Mouse clicked");
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        System.out.println("Mouse released");
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        System.out.println("Mouse dragged");
    }

    @Override
    public void mouseScrolled(int distance) {
        System.out.println("Mouse scrolled");
    }
}
