package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.io.Serializable;

public class MoveableHudElement extends Gui implements Serializable {
    public double xRatio = 0;
    public double yRatio = 0;

    public int width;
    public int height;

    public ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

    public void edit() {
        Minecraft.getMinecraft().displayGuiScreen(new MoveableHudElementEditor(this));
    }

    public void render() {
        scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        width = scaledResolution.getScaledWidth();
        height = scaledResolution.getScaledHeight();
    }
}
