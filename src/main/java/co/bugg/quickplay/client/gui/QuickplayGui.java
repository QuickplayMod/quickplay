package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public abstract class QuickplayGui extends GuiScreen {

    public float opacity = 0;

    @Override
    public void initGui() {
        super.initGui();
        fadeIn();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void fadeIn() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(opacity < 1) {
                opacity+= 0.05f;
                System.out.println("Up");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if(Mouse.getDWheel() != 0)
            mouseScrolled(Mouse.getDWheel());
    }

    public abstract void mouseScrolled(int distance);
}
