package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class QuickplayGui extends GuiScreen {

    public List<QuickplayGuiComponent> componentList = new ArrayList<>();
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
        for (QuickplayGuiComponent component : componentList) {
            component.draw(this.mc, mouseX, mouseY, opacity);
        }
    }

    @Override
    public void drawDefaultBackground() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        // Prepend opacity to 24-bit color
        drawRect(0, 0, width, height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));
        // drawRect disables blend (Grr!)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPopMatrix();
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0) {
            for(QuickplayGuiComponent component : componentList) {
                if(component.mousePressed(mc, mouseX, mouseY)) {
                    componentClicked(component);
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for(QuickplayGuiComponent component : componentList) {
            component.mouseReleased(mc, mouseX, mouseY);
        }
    }

    public abstract void mouseScrolled(int distance);

    public void componentClicked(QuickplayGuiComponent component) {
        // Play clicky sound
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}
