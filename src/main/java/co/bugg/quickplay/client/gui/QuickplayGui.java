package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class QuickplayGui extends GuiScreen {

    public List<QuickplayGuiComponent> componentList = new ArrayList<>();
    public float opacity = 0;

    int lastMouseY = 0;
    int mouseYMovement = 0;
    public QuickplayGuiContextMenu contextMenu = null;
    /**
     * Whether the GUI blurred background shader needs to be removed when the GUI closes
     * Assigned on GUI opening, otherwise if the user opens the GUI, the setting changes, and then closes the GUI,
     * the shader is applied until the user restarts the game, removes the shader manually, or re-enables the setting.
     */
    public boolean disableShaderOnGuiClose = Quickplay.INSTANCE.settings.blurGuiBackgrounds;

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if(disableShaderOnGuiClose)
            // Stop using shaders
            Minecraft.getMinecraft().entityRenderer.stopUseShader();

        // Show HUD again
        mc.gameSettings.hideGUI = false;
    }

    @Override
    public void initGui() {
        closeContextMenu();
        componentList.clear();
        super.initGui();
        if(Quickplay.INSTANCE.settings.fadeInGuis && opacity < 1)
            fadeIn();
        else opacity = 1;

        // Hide HUD (health & scoreboard & such)
        mc.gameSettings.hideGUI = true;

        // Load the blur background shader
        if(Quickplay.INSTANCE.settings.blurGuiBackgrounds)
            Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation(Reference.MOD_ID, "shaders/quickplay_gui.json"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (QuickplayGuiComponent component : componentList) {
            component.draw(this.mc, mouseX, mouseY, opacity);
        }
    }

    @Override
    public void drawDefaultBackground() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        // Prepend opacity to 24-bit color
        drawRect(0, 0, width, height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));
        // drawRect disables blend (Grr!)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
        if(textLines.size() > 0) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);

            int textXMargins = 4;
            int boxMargins = 10;
            int textYMargins = 2;
            int textWidth = 0;

            // Calculate the max width of the text
            for(String line : textLines) {
                if(font.getStringWidth(line) > textWidth)
                    textWidth = font.getStringWidth(line);
            }

            boolean sidesSwapped = false;
            if(x > width / 2) {
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
            if(x + textWidth + textXMargins * 2 + boxMargins > width || (sidesSwapped && x < boxMargins)) {
                final List<String> allWrappedLines = new ArrayList<>();
                int wrappedTextWidth = 0;
                for(String line : textLines) {
                    final int wrapWidth = sidesSwapped ? x + textWidth + textXMargins : width - x - boxMargins - textXMargins;
                    final List<String> wrappedLine = font.listFormattedStringToWidth(line, wrapWidth);

                    for(String wrappedFragment : wrappedLine) {
                        final int wrappedFragmentWidth = font.getStringWidth(wrappedFragment);
                        if(wrappedFragmentWidth > wrappedTextWidth)
                            wrappedTextWidth = wrappedFragmentWidth;

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
            int tooltipHeight = textLines.size() * (font.FONT_HEIGHT + textYMargins) + textYMargins * 2;

            // Move up if falling off bottom of screen
            if(y + tooltipHeight > height)
                y -= tooltipHeight;

            // Draw background
            drawRect(x, y, x + textWidth + textXMargins, y + tooltipHeight, (int) (opacity * 0.5 * 255) << 24);
            GL11.glEnable(GL11.GL_BLEND);

            // Draw text
            int currentLineY = y + textYMargins;
            for(String line : textLines) {
                drawString(font, line, x + textXMargins, currentLineY, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                currentLineY += font.FONT_HEIGHT + textYMargins;
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    public void closeContextMenu() {
        if(contextMenu != null) {
            if(componentList.contains(contextMenu))
                componentList.remove(contextMenu);
            contextMenu = null;
        }
    }

    public void fadeIn() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(opacity < 1) {
                opacity+= 0.05f;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int distance;
        if((distance = Mouse.getDWheel()) != 0) {
            // Divide the distance by 10 as "120" px is way too much
            mouseScrolled(distance / 10);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0)
            // Go through components in reverse order in order to process top elements first
            // Creates new copy of arraylist to avoid a ConcurrentModificationException caused by mouseClicked or componentClicked
            for(QuickplayGuiComponent component : new ArrayList<>(Lists.reverse(componentList))) {
                if(component.mouseHovering(mc, mouseX, mouseY)) {
                    if(component.mouseClicked(mc, mouseX, mouseY, mouseButton))
                        break;
                    componentClicked(component);
                }
            }

        closeContextMenu();
        // lastMouseY is used for dragging scrolling
        lastMouseY = mouseY;
    }

    private String trisTribute = "";
    private final String magicWord = "tris";
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        for(QuickplayGuiComponent component : componentList) {
            if(component.keyTyped(typedChar, keyCode))
                return;
        }

        if(keyCode == 1 || keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.displayGuiScreen(null);
        }

        // A... completely pointless tribute.
        trisTribute += typedChar;
        if(trisTribute.toUpperCase().endsWith(magicWord.toUpperCase())) {
            // Abra kadabra! Open sesame!
            Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation(Reference.MOD_ID, "shaders/quickplay_rainbow_gui.json"));
            Quickplay.INSTANCE.threadPool.submit(() -> {
                while(Minecraft.getMinecraft().currentScreen == this) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.chicken.hurt"), 1.0F));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
        }
        // /tribute
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mouseYMovement = lastMouseY - mouseY;
        lastMouseY = mouseY;
        // Scroll should be the same direction the mouse is moving
        if(mouseYMovement != 0) mouseScrolled(mouseYMovement * -1);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for(QuickplayGuiComponent component : componentList)
            component.mouseReleased(mc, mouseX, mouseY);
    }

    public abstract void mouseScrolled(int distance);

    public void componentClicked(QuickplayGuiComponent component) {
        // Play clicky sound
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}
