package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.io.Serializable;

public abstract class MoveableHudElement extends Gui implements Serializable {

    public int screenWidth;
    public int screenHeight;
    public double opacity = 1;

    public ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

    public void edit() {
        Minecraft.getMinecraft().displayGuiScreen(new MoveableHudElementEditor(this));
    }

    public void render() {
        this.render(getxRatio(), getyRatio(), 1);
    }

    public void render(double x, double y, double opacity) {
        this.opacity = opacity;
        scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        screenWidth = scaledResolution.getScaledWidth();
        screenHeight = scaledResolution.getScaledHeight();
    }

    public abstract void setxRatio(double ratio);
    public abstract void setyRatio(double ratio);

    public abstract double getxRatio();
    public abstract  double getyRatio();

    public abstract void save();
}
