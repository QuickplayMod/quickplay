package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.Minecraft;

public class InstanceDisplay extends MoveableHudElement {

    int backgroundHorizontalPadding = 4;
    int backgroungVerticalPadding = 3;

    public InstanceDisplay() {
        super();
        xRatio = 0.5;
        yRatio = 0.05;
    }

    @Override
    public void render() {
        super.render();

        String instance = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
        int stringHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(instance);

        int scaledX = (int) (xRatio * width);
        int scaledY = (int) (yRatio * height);

        drawRect(scaledX - this.backgroundHorizontalPadding - stringWidth / 2,
                scaledY - this.backgroungVerticalPadding,
                scaledX + stringWidth + this.backgroundHorizontalPadding - stringWidth / 2 - 1, // -1 due to a padding issue I don't
                                                                                                      // understand it but it's uneven without.
                scaledY + stringHeight + this.backgroungVerticalPadding,
                0x40000000);

        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, instance, scaledX, scaledY, 0xFF0000);
    }
}
