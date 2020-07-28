package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiContextMenu;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.Mode;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * GUI for individual Quickplay {@link Game}s
 */
public class QuickplayGuiGame extends QuickplayGui {
    /**
     * Game this GUI is for
     */
    public final Game game;

    /**
     * The height at which the header (game name) is rendered
     */
    public int headerHeight;
    /**
     * Scale of the header
     */
    public double headerScale = 1.5;
    /**
     * Margins on the bottom of the header between the header & the item below it
     */
    public final int headerBottomMargins = 3;
    /**
     * Scale of the game's logo
     */
    public double logoScale = 0.25;
    /**
     * UV size of the logo
     */
    public final int logoSize = 256;
    /**
     * Margins on the bottom of the logo between the logo & the item below it
     */
    public final int logoBottomMargins = 10;
    /**
     * Y level of the box in the background of the buttons
     */
    public int topOfBackgroundBox;
    /**
     * Padding between the background box's edge and the items inside it
     */
    public int backgroundBoxPadding = 10;

    // Used for button positions
    /**
     * Percentage padding between the sides of the window and the columns
     */
    public int windowPadding;
    /**
     * Number of columns the client should try to render
     */
    public int columnCount = 1;
    /**
     * The X position of the first column
     */
    public int columnZeroX;

    /**
     * The column currently being calculated by {@link #initGui()}
     */
    public int currentColumn;
    /**
     * The row currently being calculated by {@link #initGui()}
     */
    public int currentRow;

    /**
     * The margins between each mode button both vertically and horizontally
     */
    public final int buttonMargins = 5;
    /**
     * The width of each mode button
     */
    public int buttonWidth = 200;
    /**
     * The height of each mode button
     */
    public int buttonHeight = 20;

    /**
     * The string containing the current copyright
     */
    public String copyright;
    /**
     * The margins between the copyright string and the bottom of the screen
     */
    public final int copyrightMargins = 3;

    /**
     * Constructor
     *
     * @param game Game this GUI is for
     */
    public QuickplayGuiGame(Game game) {
        if(game != null) {
            this.game = game;
        } else {
            throw new IllegalArgumentException("game cannot be null.");
        }
    }

    /**
     * Constructor
     *
     * Used for keybinds, gameList is queried for the name of the game provided
     * @param unlocalizedGameName Name of the game to display if possible
     */
    @SuppressWarnings("unused")
    public QuickplayGuiGame(String unlocalizedGameName) {
        if(unlocalizedGameName != null) {
            List<Game> filteredList = Quickplay.INSTANCE.gameList
                    .stream()
                    .filter(game -> game.unlocalizedName.equals(unlocalizedGameName))
                    .collect(Collectors.toList());
            if(filteredList.size() <= 0) {
                throw new IllegalArgumentException("unlocalizedGameName could not find a matching game in gameList!");
            }
            else {
                game = filteredList.get(0);
            }
        } else {
            throw new IllegalArgumentException("unlocalizedGameName cannot be null.");
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        currentColumn = currentRow = 0;

        copyright = I18n.format("quickplay.gui.copyright", Calendar.getInstance().get(Calendar.YEAR));

        windowPadding = (int) (width * (width > 500 ? 0.25 : 0.15));
        headerHeight = (int) (height * 0.05);
        // Responsive size
        headerScale = height > 300 ? 1.5 : 1.0;
        logoScale = height > 300 ? 0.25 : 0.15;

        topOfBackgroundBox = (int) (headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins +
                logoSize * logoScale) + logoBottomMargins;

        buttonWidth = 200;
        columnCount = (int) Math.floor((double) (width - windowPadding) / (buttonWidth + buttonMargins));
        // If no full size buttons can fit on screen, then set column count back to 1 & shrink buttons
        if(columnCount < 1) {
            columnCount = 1;
            buttonWidth = width - buttonMargins * 2;
        }
        // If there are more columns than items then decrease column count
        if(columnCount > game.modes.size()) {
            columnCount = game.modes.size();
        }

        // Calculate X position of column zero
        columnZeroX = width / 2 - (buttonWidth + buttonMargins) * columnCount / 2;

        // add buttons
        for(ListIterator<Mode> iter = game.modes.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final Mode next = iter.next();
            this.addComponent(new QuickplayGuiButton(next, index, columnZeroX + (buttonWidth + buttonMargins) * currentColumn,
                    topOfBackgroundBox + backgroundBoxPadding + (buttonHeight + buttonMargins) * currentRow,
                    buttonWidth, buttonHeight, next.name, true));
            // Proceed to next position
            if(currentColumn + 1 >= columnCount) {
                currentColumn = 0;
                currentRow++;
                // Calculate new column zero X if necessary, to center items
                if(game.modes.size() < (currentRow + 1) * columnCount) {
                    columnZeroX = width / 2 - ((buttonWidth + buttonMargins) * (game.modes.size() - currentRow * columnCount)) / 2;
                }
            } else {
                currentColumn++;
            }
        }
        this.addComponent(new QuickplayGuiButton(null, game.modes.size() + 1, 3, 3, 100, 20,
                I18n.format("quickplay.gui.back"), false));

        setScrollingValues();
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        scrollFrameTop = topOfBackgroundBox + backgroundBoxPadding;

        // Increase scroll speed & amount
        scrollMultiplier = 5;
        scrollDelay = 1;
        scrollbarYMargins = 0;
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof Mode && contextMenu == null) {
            final Mode mode = (Mode) component.origin;
            // For security purposes, only actual commands are sent and chat messages can't be sent.
            if(mode.command.startsWith("/")) {
                Quickplay.INSTANCE.chatBuffer.push(mode.command);
            }

            // Send analytical data to Google
            if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                    Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("GUIs", "Game Option Pressed")
                                .setEventLabel(mode.name + " : " + mode.command)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else if(component.displayString.equals(I18n.format("quickplay.gui.back"))) {
            Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiMainMenu());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        drawDefaultBackground();

        if(opacity > 0) {
            GlStateManager.scale(headerScale, headerScale, headerScale);
            drawCenteredString(fontRendererObj, game.name, (int) (width / 2 / headerScale), (int) (headerHeight / headerScale),
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
            GlStateManager.scale(1 / headerScale, 1 / headerScale, 1 / headerScale);
        }

        GlStateManager.scale(logoScale, logoScale, logoScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,opacity);
        mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID,
                Hashing.md5().hashString(game.imageURL.toString(), StandardCharsets.UTF_8).toString() + ".png"));

        drawTexturedModalRect((float) ((width / 2 - logoSize * logoScale / 2) / logoScale),
                (float) ((headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins) / logoScale),
                0, 0, logoSize, logoSize);
        GlStateManager.enableBlend();
        GlStateManager.scale(1 / logoScale, 1 / logoScale, 1 / logoScale);

        final int columnZeroRowZeroX = width / 2 - (buttonWidth + buttonMargins) * columnCount / 2;
        final int rightOfBox = columnZeroRowZeroX + columnCount * (buttonWidth + buttonMargins) - buttonMargins + backgroundBoxPadding;

        // Draw a background behind the buttons
        //final int bottomOfBox = (int) (Math.ceil((double) game.modes.size() / columnCount) * (buttonHeight + buttonMargins) - buttonMargins + scrollFadeLine + backgroundBoxPadding);
        //drawRect(columnZeroRowZeroX - backgroundBoxPadding, topOfBackgroundBox, rightOfBox, bottomOfBox, (int) (opacity * 255 * 0.5) << 24);

        // Modified super.drawScreen()
        updateOpacity();
        final int scrollFadeDistance = 10;
        for (QuickplayGuiComponent component : componentList) {
            double scrollOpacity = component.scrollable ? ((component.y - scrollPixel) > topOfBackgroundBox +
                    backgroundBoxPadding ? 1 : (component.y - scrollPixel) + scrollFadeDistance < topOfBackgroundBox +
                    backgroundBoxPadding ? 0 : (scrollFadeDistance - ((double) topOfBackgroundBox + backgroundBoxPadding -
                    (double) (component.y - scrollPixel))) / (double) scrollFadeDistance) : 1;
            if(opacity * scrollOpacity > 0) {
                component.draw(this, mouseX, mouseY, opacity * scrollOpacity);
            }
        }

        drawScrollbar(rightOfBox);

        if(opacity > 0) {
            drawCenteredString(fontRendererObj, copyright, width / 2, height - fontRendererObj.FONT_HEIGHT - copyrightMargins,
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if(Quickplay.INSTANCE.settings.anyKeyClosesGui) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(QuickplayGuiComponent component : componentList) {
            if (!(component instanceof QuickplayGuiContextMenu) && component.mouseHovering(this, mouseX, mouseY) && mouseButton == 1) {
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(new String[]{I18n.format("quickplay.gui.favorite")}),
                        component, -1, mouseX, mouseY) {
                    @Override
                    public void optionSelected(int index) {
                        if (index == 0) {
                            if (component.origin instanceof Mode) {// Open key binding GUI & add new keybind
                                Quickplay.INSTANCE.keybinds.keybinds.add(new QuickplayKeybind(game.name + " " +
                                        ((Mode) component.origin).name, Keyboard.KEY_NONE, ((Mode) component.origin).command));
                            }
                            try {
                                Quickplay.INSTANCE.keybinds.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Quickplay.INSTANCE.sendExceptionRequest(e);
                            }
                            Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());
                        }
                        closeContextMenu();
                    }
                };
                this.addComponent(contextMenu);
                break;
            }
        }
    }
}
