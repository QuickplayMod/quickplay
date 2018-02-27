package co.bugg.quickplay.client.gui;

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
    public boolean scrollable;

    public QuickplayGuiComponent(Object origin, int id, int x, int y, int width, int height, String displayString, boolean scrollable) {
        opacity = 1.0;
        this.origin = origin;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayString = displayString;
        this.scrollable = scrollable;
    }

    public synchronized void move(int distance) {
        this.y = y + distance;
    }

    public abstract void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity);

    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;
        return (mouseX > x && mouseX < (x + width)) && (mouseY > scrollAdjustedY && mouseY < (scrollAdjustedY + height));
    }
    public abstract void mouseReleased(QuickplayGui gui, int mouseX, int mouseY);

    /**
     * Called whenever a key is typed in a QuickplayGui on all elements in componentList
     * @param keyTyped Character that was typed
     * @param keyCode LWJGL key code
     * @return Whether the key press should be cancelled (true to cancel)
     */
    public abstract boolean keyTyped(char keyTyped, int keyCode);

    /**
     * Called whenever a mouse is pressed in a QuickplayGui on all elements in componentList
     * @param gui the GUI this component is being rendered on
     * @param mouseX X position of the mouse
     * @param mouseY Y position of the mouse
     * @param mouseButton mouse button clicked
     * @return Whether the click should be cancelled (true to cancel)
     */
    public abstract boolean mouseClicked(QuickplayGui gui, int mouseX, int mouseY, int mouseButton);
}
