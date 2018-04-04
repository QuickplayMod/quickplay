package co.bugg.quickplay.client.gui.components;

import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.client.gui.Gui;

/**
 * Basic component class for almost all Quickplay GUI elements
 */
public abstract class QuickplayGuiComponent extends Gui {

    /**
     * Width of this component
     */
    public int width;
    /**
     * Height of this component
     */
    public int height;
    /**
     * X location of the left of this component
     * If {@link #scrollable} is true, this value does not get updated.
     * Instead it is up to the GUI to add or subtract the current distance scrolled
     * This value will remain constant, as if scrolled distance is equal to 0.
     */
    public int x;
    /**
     * Y location of the top of this component
     * If {@link #scrollable} is true, this value does not get updated.
     * Instead it is up to the GUI to add or subtract the current distance scrolled
     * This value will remain constant, as if scrolled distance is equal to 0.
     */
    public int y;
    /**
     * This component's ID. Should be unique but usually isn't necessary
     */
    public int id;
    /**
     * Label string to be drawn for this component if necessary
     */
    public String displayString;
    /**
     * Whether the user is currently hovering over this component or not
     */
    public boolean hovering;
    /**
     * The arbitrary origin of this component
     * Can be used however the user finds necessary. Is a useful alternative to keeping track of IDs
     * e.g. instead of remembering that id 3 points to the Skywars {@link co.bugg.quickplay.games.Game}, the user can
     * just set the {@link co.bugg.quickplay.games.Game} as the <code>origin</code>.
     */
    public Object origin;
    /**
     * Whether this component can be scrolled up and down in GUIs or should be fixed
     */
    public boolean scrollable;

    /**
     * Constructor
     *
     * @param origin The origin of this component
     * @param id The ID of this component
     * @param x The X location of this component when scrolling = 0
     * @param y The Y location of this component when scrolling = 0
     * @param width The width of this component
     * @param height The height of this component
     * @param displayString The string displayed along with this component
     * @param scrollable Whether this component should scroll up and down or not
     */
    public QuickplayGuiComponent(Object origin, int id, int x, int y, int width, int height, String displayString, boolean scrollable) {
        this.origin = origin;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayString = displayString;
        this.scrollable = scrollable;
    }

    /**
     * Move this component vertically a certain distance
     * @param distance the distance to move this component. Distance is added (positive values move the component down).
     */
    public synchronized void move(int distance) {
        this.y = y + distance;
    }

    /**
     * Draw ths component on the screen
     * @param gui GUI this component is being drawn on
     * @param mouseX The X position of the mouse at the moment
     * @param mouseY The Y position of the mouse at the moment
     * @param opacity The opacity to draw this component at
     */
    public abstract void draw(QuickplayGui gui, int mouseX, int mouseY, double opacity);

    /**
     * Calculates whether the mouse is currently hovering over this component or not
     * @param gui GUI this component is being drawn on
     * @param mouseX The X position of the mouse
     * @param mouseY The Y position of the mouse
     * @return By default, Whether the X and Y positions of the mouse are within the width & height of this component.
     */
    public boolean mouseHovering(QuickplayGui gui, int mouseX, int mouseY) {
        final int scrollAdjustedY = scrollable ? y - gui.scrollPixel : y;
        return (mouseX > x && mouseX < (x + width)) && (mouseY > scrollAdjustedY && mouseY < (scrollAdjustedY + height));
    }

    /**
     * Called whenever the mouse is released from a click or click-drag
     * @param gui GUI this component is being drawn on
     * @param mouseX The X position of the mouse when the mouse was released
     * @param mouseY the Y position of the mouse when the mouse was released
     */
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
