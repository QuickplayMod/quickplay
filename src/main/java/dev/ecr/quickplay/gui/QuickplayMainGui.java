package dev.ecr.quickplay.gui;

import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.Window;
import gg.essential.elementa.constraints.AspectConstraint;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UScreen;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class QuickplayMainGui extends UScreen {

    private static final Window window = new Window(ElementaVersion.V7);

    @Override
    public void initScreen(int width, int height) {
        super.initScreen(width, height);
        UIBlock block = new UIBlock();
        block.setX(new CenterConstraint());
        block.setY(new PixelConstraint(10f));
        block.setWidth(new PixelConstraint(100f));
        block.setHeight(new PixelConstraint(100f));
        block.setColor(new Color(30, 255, 0));
        block.setChildOf(window);

        for(int i = 0; i < 15; i++) {
            try {
                UIImage image = UIImage.Companion.ofURL(new URL("https://bugg.co/quickplay/images/games/platform-pc-256.png"));
                image.setWidth(new RelativeConstraint(0.1f));
                image.setX(new RelativeConstraint(0.1f * i));
                image.setHeight(new AspectConstraint(1.0f));
                image.onMouseClickConsumer(mouseEvent -> {
                    System.out.println("Test");
                });
                image.setChildOf(window);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDrawScreen(@NotNull UMatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks);
        window.draw(matrixStack);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        window.mouseClick(mouseX, mouseY, mouseButton);
    }
}
