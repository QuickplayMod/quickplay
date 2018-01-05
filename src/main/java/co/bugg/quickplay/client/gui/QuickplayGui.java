package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public abstract class QuickplayGui extends GuiScreen {

    public float opacity = 0;

    @Override
    public void initGui() {
        super.initGui();
        if(Quickplay.INSTANCE.settings.fadeInGuis)
            fadeIn();
        else opacity = 1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            ((QuickplayGuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY, opacity);
        }

        for (int j = 0; j < this.labelList.size(); ++j)
        {
            ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }
    }

    public void fadeIn() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(opacity < 1) {
                opacity+= 0.05f;
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

        int distance;
        if((distance = Mouse.getDWheel()) != 0) {
            mouseScrolled(distance);
        }
    }

    public abstract void mouseScrolled(int distance);
}
