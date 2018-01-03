package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class InstanceDisplay extends MoveableHudElement {

    int backgroundHorizontalPadding = 4;
    int backgroungVerticalPadding = 3;

    public InstanceDisplay() {
        super();
    }

    @Override
    public void render(double x, double y) {
        super.render(x, y);

        String instance = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
        int stringHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(instance);

        int scaledX = (int) (x * screenWidth);
        int scaledY = (int) (y * screenHeight);

        drawRect(scaledX - this.backgroundHorizontalPadding - stringWidth / 2,
                scaledY - this.backgroungVerticalPadding,
                scaledX + stringWidth + this.backgroundHorizontalPadding - stringWidth / 2 - 1, // -1 due to a padding issue I don't
                                                                                                      // understand it but it's uneven without.
                scaledY + stringHeight + this.backgroungVerticalPadding,
                0x40000000);

        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, instance, scaledX, scaledY, Quickplay.INSTANCE.settings.primaryColor.getRGB());
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
        // TODO null pointer check
        return Quickplay.INSTANCE.settings.instanceDisplayX;
    }

    @Override
    public double getyRatio() {
        // TODO null pointer check
        return Quickplay.INSTANCE.settings.instanceDisplayY;
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
