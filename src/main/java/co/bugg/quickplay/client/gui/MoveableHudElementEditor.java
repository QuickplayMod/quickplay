package co.bugg.quickplay.client.gui;

import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class MoveableHudElementEditor extends GuiScreen {

    private final MoveableHudElement element;

    public MoveableHudElementEditor(MoveableHudElement element) {
        this.element = element;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        element.render();

        super.drawScreen(mouseX, mouseY, partialTicks);
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
        // TODO Save config
    }

    public void moveTo(int mouseX, int mouseY) {
        element.xRatio = (double) mouseX / (double) element.width;
        element.yRatio = (double) mouseY / (double) element.height;
    }
}
