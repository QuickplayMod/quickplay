package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
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

    int lastMouseY = 0;
    int mouseYMovement = 0;

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Stop using shaders
        Minecraft.getMinecraft().entityRenderer.stopUseShader();
        // Show HUD again
        mc.gameSettings.hideGUI = false;
    }

    @Override
    public void initGui() {
        super.initGui();
        if(Quickplay.INSTANCE.settings.fadeInGuis)
            fadeIn();
        else opacity = 1;

        // Hide HUD (health & scoreboard & such)
        mc.gameSettings.hideGUI = true;

        // Load the blur background shader
        if(Quickplay.INSTANCE.settings.blurGuiBackgrounds)
            Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation(Reference.MOD_ID, "shaders/quickplay_gui.json"));
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
            // Divide the distance by 10 as "120" px is way too much
            mouseScrolled(distance / 10);
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

        // lastMouseY is used for dragging scrolling
        lastMouseY = mouseY;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if(keyCode == 1 || keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mouseYMovement = lastMouseY - mouseY;
        lastMouseY = mouseY;
        System.out.println(mouseYMovement);
        // Scroll should be the same direction the mouse is moving
        if(mouseYMovement != 0) mouseScrolled(mouseYMovement * -1);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for(QuickplayGuiComponent component : componentList)
            component.mouseReleased(mc, mouseX, mouseY);
    }

    public abstract void mouseScrolled(int distance);

    public void componentClicked(QuickplayGuiComponent component) {
        // Play clicky sound
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}
