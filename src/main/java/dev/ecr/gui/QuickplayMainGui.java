package dev.ecr.gui;

import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UScreen;
import org.jetbrains.annotations.NotNull;

public class QuickplayMainGui extends UScreen {

    @Override
    public void onDrawScreen(@NotNull UMatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks);
        super.drawString(UMinecraft.getFontRenderer(), "Hello, world!", mouseX, mouseY, 0xff0000ff);
    }
}
