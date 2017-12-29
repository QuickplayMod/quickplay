package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;

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
        int stringHeight = fontRenderer.FONT_HEIGHT;
        int stringWidth = fontRenderer.getStringWidth(instance);

        int scaledX = (int) (xRatio * width);
        int scaledY = (int) (yRatio * height);

        drawRect(scaledX - this.backgroundHorizontalPadding - stringWidth / 2,
                scaledY - this.backgroungVerticalPadding,
                scaledX + stringWidth + this.backgroundHorizontalPadding - stringWidth / 2 - 1, // -1 due to a padding issue I don't
                                                                                                      // understand it but it's uneven without.
                scaledY + stringHeight + this.backgroungVerticalPadding,
                0x40000000);

        drawCenteredString(fontRenderer, instance, scaledX, scaledY, 0xFF0000);
    }
}
