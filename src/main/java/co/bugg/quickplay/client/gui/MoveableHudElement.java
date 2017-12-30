package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.io.Serializable;

public abstract class MoveableHudElement extends Gui implements Serializable {

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

    public abstract void setxRatio(double ratio);
    public abstract void setyRatio(double ratio);

    public abstract double getxRatio();
    public abstract  double getyRatio();

    public abstract void save();
}
