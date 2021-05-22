package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.animations.Animation;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiContextMenu;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Basic GUI screen for all Quickplay GUIs
 *
 * NEVER override Minecraft methods when extending off of this Class.
 * ALWAYS override Quickplay's methods, or create your own if one does not exist.
 */
public abstract class QuickplayGui extends GuiScreen {

    /**
     * Current vertical pixel index that the scrollable content is at at {@link #scrollFrameTop}
     */
    public int scrollPixel = 0;
    /**
     * Multiplier for the number of pixels the screen should scroll per call to {@link #mouseScrolled(int)}
     */
    public double scrollMultiplier = 2;
    /**
     * Number of ms between "scroll frames"
     * Absolute value is taken to avoid negatives.
     */
    public int scrollDelay = 2;
    /**
     * How many pixels high the content that can be scrolled is
     * Includes {@link #scrollContentMargins}
     */
    public int scrollContentHeight = 0;
    /**
     * Vertical Y margins on the scrollable content.
     * DOES NOT include both the top and bottom. If you want the margins
     * split evenly on both top & bottom then you need this value / 2
     *
     * Coder is responsible for drawing items in the correct place, similar to
     * {@link #scrollFrameTop} and {@link #scrollFrameBottom}
     *
     * {@link #scrollContentHeight} gets this value added to it
     * @see #scrollFrameTop
     * @see #scrollFrameBottom
     */
    public int scrollContentMargins = 30;
    /**
     * Top of the frame for scrollable content
     * When scrolling up, content SHOULD stop at this pixel
     *
     * If content is not drawn at this line to begin with, unexpected results may come.
     *
     * Default <code>0</code>, as set in {@link #initGui()}
     */
    public int scrollFrameTop;
    /**
     * Bottom of the frame for scrollable content
     * When scrolling down, content SHOULD stop at this pixel
     *
     * If {@link #calcScrollHeight()} is overridden, unexpected results may come.
     *
     * Default <code>height</code>, as set in {@link #initGui()}
     */
    public int scrollFrameBottom;
    /**
     * Width in pixels of the scrollbar
     *
     * @see #drawScrollbar(int)
     */
    public int scrollbarWidth = 3;
    /**
     * Default scrollbar vertical margins
     * to avoid the scrollbar touching the edges of the frame
     */
    public int scrollbarYMargins = 5;
    /**
     * The list of all components that should be rendered
     */
    public List<QuickplayGuiComponent> componentList = new ArrayList<>();
    /**
     * The opacity of this screen between 0 and 1
     */
    public float opacity = 0;

    /**
     * Y location of the mouse when the mouse was last clicked
     * Used for scrolling via dragging
     *
     * @see #mouseClicked(int, int, int)
     * @see #mouseScrolled(int)
     * @see #mouseClickMove(int, int, int, long)
     */
    int lastMouseY = 0;
    /**
     * The currently open right-click context menu
     * <code>null</code> if none open
     *
     * Use {@link #closeContextMenu()} to close the context menu instead of just
     * setting this to <code>null</code>, otherwise the context menu will not close.
     */
    public QuickplayGuiContextMenu contextMenu = null;
    /**
     * Whether the GUI blurred background shader needs to be removed when the GUI closes
     * Assigned on GUI opening, otherwise if the user opens the GUI, the setting changes, and then closes the GUI,
     * the shader is applied until the user restarts the game, removes the shader manually, or re-enables the setting.
     */
    public boolean disableShaderOnGuiClose = Quickplay.INSTANCE.settings.blurGuiBackgrounds;
    /**
     * Animation for GUI fade-in
     */
    public Animation fadeAnimation = new Animation(200);

    /**
     * Quickplay hook
     */
    public void hookInit() {
        // Send analytical data to Google
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.ga.createEvent("GUIs", "GUI Initialized")
                            .setEventLabel(getClass().getName())
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        this.closeContextMenu();
        this.componentList.clear();

        this.scrollPixel = 0;

        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(Quickplay.INSTANCE.settings.fadeInGuis && this.opacity < 1) {
                this.fadeAnimation.start();
            } else {
                this.opacity = 1;
            }
        });

        // Hide HUD (health & scoreboard & such)
        this.mc.gameSettings.hideGUI = true;

        // Load the blur background shader
        if(Quickplay.INSTANCE.settings.blurGuiBackgrounds) {

            QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                // This method isn't public in some versions of Forge seemingly.
                // Reflection is used just in case
                Method loadShaderMethod = null;
                try {
                    loadShaderMethod = EntityRenderer.class.getDeclaredMethod("loadShader", ResourceLocation.class);
                } catch (NoSuchMethodException e) {
                    try {
                        loadShaderMethod = EntityRenderer.class.getDeclaredMethod("func_175069_a", ResourceLocation.class);
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                        Quickplay.INSTANCE.sendExceptionRequest(e);
                    }
                }

                if (loadShaderMethod != null) {
                    loadShaderMethod.setAccessible(true);
                    try {
                        loadShaderMethod.invoke(Minecraft.getMinecraft().entityRenderer,
                                new ResourceLocation(Reference.MOD_ID, "shaders/quickplay_gui.json"));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        Quickplay.INSTANCE.sendExceptionRequest(e);
                    }
                }

            });
        }

        this.setScrollingValues();
    }
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        this.updateOpacity();

        //noinspection ForLoopReplaceableByForEach
        for (ListIterator<QuickplayGuiComponent> iter = this.componentList.listIterator(); iter.hasNext();) {
            final QuickplayGuiComponent component = iter.next();
            component.draw(this, mouseX, mouseY, this.opacity);
        }
    }

    public void hookClosed() {
        // Send analytical data to Google
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.ga.createEvent("GUIs", "GUI Closed")
                            .setEventLabel(getClass().getName())
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if(this.disableShaderOnGuiClose) {
            // Stop using shaders
            QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                Minecraft.getMinecraft().entityRenderer.stopUseShader();
            });
        }

        // Show HUD again
        this.mc.gameSettings.hideGUI = false;
    }

    public boolean hookMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {    // Go through components in reverse order in order to process top elements first
            // Creates new copy of arraylist to avoid a ConcurrentModificationException caused by mouseClicked or componentClicked
            for (final QuickplayGuiComponent component : new ArrayList<>(Lists.reverse(this.componentList))) {
                if (component.isMouseHovering(this, mouseX, mouseY)) {
                    if (component.hookMouseClicked(this, mouseX, mouseY, mouseButton)) {
                        break;
                    }
                    this.componentClicked(component);
                }
            }
        }
        this.closeContextMenu();
        // lastMouseY is used for dragging scrolling
        this.lastMouseY = mouseY;
        return false;
    }
    public boolean hookKeyTyped(char typedChar, int keyCode) {
        for(QuickplayGuiComponent component : this.componentList) {
            if(component.hookKeyTyped(typedChar, keyCode)) {
                return false;
            }
        }

        if(keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            Quickplay.INSTANCE.minecraft.openGui(null);
        }
        return false;
    }

    public boolean hookMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        final int mouseYMovement = this.lastMouseY - mouseY;
        this.lastMouseY = mouseY;
        // Scroll should be the same direction the mouse is moving
        if(mouseYMovement != 0) {
            this.mouseScrolled(mouseYMovement * -1);
        }
        return false;
    }

    public boolean hookMouseReleased(int mouseX, int mouseY, int state) {
        for(final QuickplayGuiComponent component : this.componentList) {
            component.hookMouseReleased(this, mouseX, mouseY);
        }
        return false;
    }

    @Override
    public void onGuiClosed() {
        this.hookClosed();
    }

    @Override
    public void initGui() {
        this.hookInit();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hookRender(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawDefaultBackground() {
        if(!Quickplay.INSTANCE.settings.transparentBackgrounds) {
            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();
            // Prepend opacity to 24-bit color
            QuickplayGui.drawRect(0, 0, this.getWidth(), this.getHeight(),
                    0x000000 | ((int) (this.opacity * 0.5 * 255) << 24));
            // drawRect disables blend (Grr!)
            GlStateManagerWrapper.enableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }

    @Override
    protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
        if(textLines.size() > 0 && this.opacity > 0) {
            GlStateManagerWrapper.pushMatrix();
            GlStateManagerWrapper.enableBlend();

            int textXMargins = 6;
            int boxMargins = 10;
            int textYMargins = 2;
            int textWidth = 0;

            // Calculate the max width of the text
            for(String line : textLines) {
                if(font.getStringWidth(line) > textWidth) {
                    textWidth = font.getStringWidth(line);
                }
            }

            boolean sidesSwapped = false;
            if(x > this.getWidth() / 2) {
                // Move the text over to the other side
                x -= textWidth + textXMargins * 2;
                // Subtract margins between the box and the cursor
                x -= boxMargins;
                // Side of the screen/mouse the text is rendered on is swapped
                // This is used for line wrapping
                sidesSwapped = true;
            } else {
                // Add margins between the box and the cursor
                x += boxMargins;
            }

            // Wrap all the lines if necessary
            if(x + textWidth + textXMargins * 2 + boxMargins > this.getWidth() || (sidesSwapped && x < boxMargins)) {
                final List<String> allWrappedLines = new ArrayList<>();
                int wrappedTextWidth = 0;
                for(String line : textLines) {
                    final int wrapWidth = sidesSwapped ? x + textWidth + textXMargins : this.getWidth() - x - boxMargins - textXMargins;
                    final List<String> wrappedLine = font.listFormattedStringToWidth(line, wrapWidth);

                    for(String wrappedFragment : wrappedLine) {
                        final int wrappedFragmentWidth = font.getStringWidth(wrappedFragment);
                        if(wrappedFragmentWidth > wrappedTextWidth) {
                            wrappedTextWidth = wrappedFragmentWidth;
                        }

                        allWrappedLines.add(wrappedFragment);
                    }
                }
                // Recalcuate x if sides swapped
                if(sidesSwapped) {
                    x += textWidth - wrappedTextWidth;
                }

                textWidth = wrappedTextWidth;
                textLines = allWrappedLines;

            }
            // Calculate how high the tooltip should be
            int tooltipHeight = textLines.size() * (this.getFontHeight() + textYMargins) + textYMargins;

            // Move up if falling off bottom of screen
            if(y + tooltipHeight > this.getHeight()) {
                y -= tooltipHeight;
            }

            // Draw background
            drawRect(x, y, x + textWidth + textXMargins, y + tooltipHeight, (int) (this.opacity * 0.5 * 255) << 24);
            GlStateManagerWrapper.enableBlend();

            // Draw text
            int currentLineY = y + textYMargins;
            for(String line : textLines) {
                drawString(font, line, x + textXMargins / 2, currentLineY,
                        Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                currentLineY += this.getFontHeight() + textYMargins;
            }

            GlStateManagerWrapper.disableBlend();
            GlStateManagerWrapper.popMatrix();
        }
    }

    /**
     * Closes the currently open context menu, if there is one open.
     */
    public void closeContextMenu() {
        if(this.contextMenu != null) {
            this.removeComponent(this.contextMenu);
            this.contextMenu = null;
        }
    }

    /**
     * Update the opacity/animation of this GUI
     */
    public void updateOpacity() {
        if(this.fadeAnimation != null && this.fadeAnimation.started) {
            this.fadeAnimation.updateFrame();
            this.opacity = (float) this.fadeAnimation.progress;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int distance;
        if((distance = Mouse.getDWheel()) != 0) {
            // Divide the distance by 10 as "120" px is way too much
            this.mouseScrolled(distance / 10);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.hookMouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(this.hookKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(this.hookMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.hookMouseReleased(mouseX, mouseY, state)) {
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Default calculation for top of the screen's scrolling limit
     * Finds the gui component with the lowest Y value (highest on screen)
     * @return the Y value of the highest component on the user's screen minus {@link #scrollContentMargins}
     */
    public int calcScrollHeight() {
        if(this.componentList.size() > 0) {
            // component with the highest Y value
            QuickplayGuiComponent lowestComponent = null;
            // component with has the lowest Y value
            QuickplayGuiComponent highestComponent = null;
            for (final QuickplayGuiComponent component : this.componentList) {
                if (component.scrollable && (highestComponent == null || highestComponent.y > component.y)) {
                    highestComponent = component;
                } else if (component.scrollable && (lowestComponent == null || lowestComponent.y < component.y)) {
                    lowestComponent = component;
                }
            }

            if(highestComponent != null && lowestComponent != null) {
                return lowestComponent.y - highestComponent.y + lowestComponent.height + this.scrollContentMargins;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the GUI's scrolling values to determine
     * where the user can scroll & such
     */
    public void setScrollingValues() {
        // Default scrollable area Y padding. Can be changed
        this.scrollContentMargins = 30;
        // Calculate the height of the scrollable content
        this.scrollContentHeight = this.calcScrollHeight();
        // Top & bottom of thee scroll frame
        this.scrollFrameBottom = this.getHeight();
        this.scrollFrameTop = 0;
    }

    /**
     * Getter for this.width
     * @return this.width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Getter for this.height
     * @return this.height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Getter for this.fontRendererObj.FONT_HEIGHT
     * @return this.fontRendererObj.FONT_HEIGHT
     */
    public int getFontHeight() {
        return Quickplay.INSTANCE.minecraft.mc.fontRendererObj.FONT_HEIGHT;
    }

    /**
     * Calculate the width of the provided string using the font renderer.
     * @param str String to get width of
     * @return The width of the passed string
     */
    public int getStringWidth(final String str) {
        return Quickplay.INSTANCE.minecraft.mc.fontRendererObj.getStringWidth(str);
    }

    /**
     * Called whenever the mouse is scrolled or the screen is dragged
     * @param distance Distance to scroll
     */
    public void mouseScrolled(int distance) {
        if(distance != 0) {
            // Scroll is animated so we run on a thread
            Quickplay.INSTANCE.threadPool.submit(() -> {
                // Get the scrolling direction, multiply distance by -1 to reverse it if the user's settings say so
                final boolean scrollingUp = 0 < distance * (Quickplay.INSTANCE.settings.reverseScrollingDirection ? -1 : 1);

                // Loop for the number of pixels scrolled
                for (int i = 0; i < Math.abs(distance * this.scrollMultiplier); i++) {

                    // If scrolling down & the height of the content (basically the bottom) has passed scrollFrameBottom
                    if ((!scrollingUp && this.scrollContentHeight > this.scrollPixel + (this.scrollFrameBottom - this.scrollFrameTop)) ||
                        // OR if scrolling up & the content is back to it's original position
                        (scrollingUp && 0 < this.scrollPixel)) {

                        if(scrollingUp) {
                            this.scrollPixel--;
                        } else {
                            this.scrollPixel++;
                        }

                        try {
                            Thread.sleep(Math.abs(this.scrollDelay));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        // Already reached the bottom/top, so stop trying to scroll
                        break;
                    }
                }

            });
        }
    }

    /**
     * Draw the scrollbar on screen
     * @param x X column of the left of the scrollbar
     */
    public void drawScrollbar(final int x) {
        final int scrollbarSectionHeight = this.scrollFrameBottom - this.scrollFrameTop - this.scrollbarYMargins;
        if(scrollbarSectionHeight > 0) {

            final double scrollbarRatio = (double) scrollbarSectionHeight / (double) this.scrollContentHeight;

            // Don't draw scrollbar if everything fits on screen
            if(scrollbarRatio < 1) {
                // Height of the scrollbar
                final int scrollbarHeight = (int) (scrollbarRatio * scrollbarSectionHeight);
                // How many pixels down the scrollbar should be moved
                final int scrollbarMoved = (int) (scrollbarRatio * this.scrollPixel);
                QuickplayGui.drawRect(x,
                        this.scrollFrameTop + scrollbarMoved + this.scrollbarYMargins,
                        x + this.scrollbarWidth,
                        this.scrollFrameTop + scrollbarHeight + scrollbarMoved,
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
            }
        }
    }

    /**
     * Called whenever a component in {@link #componentList} is clicked
     * @param component Component that is clicked
     */
    public void componentClicked(QuickplayGuiComponent component) {
        // Play clicky sound
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    /**
     * Add a component to this GUI's component list before next screen render.
     * Note that this could result in unexpected behavior if used in initGui directly.
     * This is intended to avoid ConcurrentModificationExceptions, but it's unclear if it'll be effective.
     * @param component The component to add
     */
    public void addComponent(final QuickplayGuiComponent component) {
        // Add components pre-render to avoid concurrent modification exception
        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> this.componentList.add(component));
    }

    /**
     * Remove a component from this GUI's component list before next screen render.
     * Note that this could result in unexpected behavior if used in initGui directly.
     * This is intended to avoid ConcurrentModificationExceptions, but it's unclear if it'll be effective.
     * @param component The component to remove
     */
    public void removeComponent(final QuickplayGuiComponent component) {
        // Remove components pre-render to avoid concurrent modification exception
        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> this.componentList.remove(component));
    }

    public void drawCenteredString(String text, int x, int y, int color) {
        super.drawCenteredString(Quickplay.INSTANCE.minecraft.mc.fontRendererObj, text, x, y, color);
    }

    public void drawString(String text, int x, int y, int color) {
        super.drawString(Quickplay.INSTANCE.minecraft.mc.fontRendererObj, text, x, y, color);
    }

    @Override
    public void drawHoveringText(List<String> lines, int x, int y) {
        this.drawHoveringText(lines, x, y, Quickplay.INSTANCE.minecraft.mc.fontRendererObj);
    }

    public List<String> listFormattedStringToWidth(String str, int maxWidth) {
        return this.fontRendererObj.listFormattedStringToWidth(str, maxWidth);
    }
}
