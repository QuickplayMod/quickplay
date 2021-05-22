package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.HypixelInstanceWatcher;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;

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
    HypixelInstanceWatcher source;

    /**
     * Constructor
     */
    public InstanceDisplay(HypixelInstanceWatcher watcher) {
        super();
        this.source = watcher;
    }

    @Override
    public void render(double x, double y, double opacity) {
        if(!Quickplay.INSTANCE.isEnabled) {
            return;
        }

        super.render(x, y, opacity);

        final String instance = this.source.getCurrentServer();
        final int stringHeight = this.getFontHeight();
        final int stringWidth = this.getStringWidth(instance);

        final double scale = Quickplay.INSTANCE.settings.instanceDisplayScale.getScale();
        final int scaledX = (int) (x * this.screenWidth / scale);
        final int scaledY = (int) (y * this.screenHeight / scale);

        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.scale(scale);

        QuickplayGui.drawRect((scaledX - this.backgroundHorizontalPadding - stringWidth / 2),
                (scaledY - this.backgroungVerticalPadding),
                /* -1 due to a padding issue I don't understand it but it's uneven without. */
                (scaledX + stringWidth + this.backgroundHorizontalPadding - stringWidth / 2 - 1),
                (scaledY + stringHeight + this.backgroungVerticalPadding),
                0x000000 | (int) (this.opacity * 100 * 0.5) << 24);
        GlStateManagerWrapper.enableBlend();

        this.drawCenteredString(instance, scaledX, scaledY, Quickplay.INSTANCE.settings.primaryColor
                .getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);

        GlStateManagerWrapper.scale(1 / scale);
        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
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
