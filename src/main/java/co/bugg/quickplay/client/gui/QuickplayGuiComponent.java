package co.bugg.quickplay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public abstract class QuickplayGuiComponent extends Gui {

    public int width;
    public int height;
    public int x;
    public int y;
    public int id;
    public String displayString;
    public boolean hovering;
    public double opacity;
    public Object origin;

    public QuickplayGuiComponent(Object origin, int id, int x, int y, int width, int height, String displayString) {
        opacity = 1.0;
        this.origin = origin;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayString = displayString;
    }

    public synchronized void move(int distance) {
        this.y = y + distance;
    }

    public abstract void draw(Minecraft mc, int mouseX, int mouseY, double opacity);

    public abstract boolean mouseHovering(Minecraft mc, int mouseX, int mouseY);
    public abstract void mouseReleased(Minecraft mc, int mouseX, int mouseY);
}
