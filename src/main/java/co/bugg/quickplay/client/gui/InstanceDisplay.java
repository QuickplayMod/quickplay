package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.InstanceWatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

/**
 * Displays the Hypixel instance the client is currently connected to
 */
public class InstanceDisplay extends MoveableHudElement {

    /**
     * Horizontal padding for the transparent background
     */
    int backgroundHorizontalPadding = 4;
    /**
     * Vertical padding for the transparent background
     */
    int backgroungVerticalPadding = 3;
    /**
     * InstanceWatcher, source of the current instance.
     */
    InstanceWatcher source;

    /**
     * Constructor
     */
    public InstanceDisplay(InstanceWatcher watcher) {
        super();
        this.source = watcher;
    }

    @Override
    public void render(double x, double y, double opacity) {
        super.render(x, y, opacity);

        final String instance = this.source.getCurrentServer();
        final int stringHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        final int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(instance);

        final double scale = Quickplay.INSTANCE.settings.instanceDisplayScale.getScale();
        final int scaledX = (int) (x * screenWidth / scale);
        final int scaledY = (int) (y * screenHeight / scale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);

        drawRect((scaledX - this.backgroundHorizontalPadding - stringWidth / 2),
                (scaledY - this.backgroungVerticalPadding),
                /* -1 due to a padding issue I don't understand it but it's uneven without. */
                (scaledX + stringWidth + this.backgroundHorizontalPadding - stringWidth / 2 - 1),
                (scaledY + stringHeight + this.backgroungVerticalPadding),
                0x000000 | (int) (opacity * 100 * 0.5) << 24);
        GlStateManager.enableBlend();

        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, instance, scaledX, scaledY,
                Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

        GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void setxRatio(double xRatio) {
        Quickplay.INSTANCE.settings.instanceDisplayX = xRatio;
    }

    @Override
    public void setyRatio(double yRatio) {
        Quickplay.INSTANCE.settings.instanceDisplayY = yRatio;
    }

    @Override
    public double getxRatio() {
        if(Quickplay.INSTANCE.settings != null) {
            return Quickplay.INSTANCE.settings.instanceDisplayX;
        }
        else return 0.5;
    }

    @Override
    public double getyRatio() {
        if(Quickplay.INSTANCE.settings != null) {
            return Quickplay.INSTANCE.settings.instanceDisplayY;
        }
        else return 0.05;
    }

    @Override
    public void save() {
        try {
            Quickplay.INSTANCE.settings.save();
        } catch (IOException e) {
            System.out.println("Error saving config!");
            e.printStackTrace();
        }
    }
}
