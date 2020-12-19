package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.*;
import co.bugg.quickplay.actions.serverbound.ButtonPressedAction;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiContextMenu;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.util.ServerUnavailableException;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * GUI for individual Quickplay {@link Screen}s
 */
public class QuickplayGuiScreen extends QuickplayGui {
    /**
     * Screen this is for
     */
    public final Screen screen;

    /**
     * UV size of each game's image
     */
    final int gameImgSize = 256;

    // Margins & padding for GUI elements
    /**
     * Vertical padding between each game element (image + text)
     * Minimum of 10
     */
    final int BoxYPadding = Math.max((int) (10 * Quickplay.INSTANCE.settings.gameLogoScale), 10);
    /**
     * Margins between the game's image and the game's title string
     */
    final int stringLeftMargins = 15;
    /**
     * Horizontal margins between each game element (image + text)
     */
    final int boxXMargins = 10;
    /**
     * Padding between the edge of the window and the game images
     */
    final int windowXPadding = 20;
    /**
     * Copyright text at the bottom of the screen
     */
    String copyright;
    /**
     * Margins between the copyright text & the bottom of the window
     */
    final int copyrightMargins = 3;
    /**
     * Longest display string for any of the games
     */
    int longestStringWidth = 0;
    /**
     * Base multiplier for scaling. The user's settings multiplier is multiplied by this
     */
    final double baseScaleMultiplier = 0.25;
    /**
     * Finalized multiplier, taking the user's settings into account
     */
    final double scaleMultiplier = baseScaleMultiplier * Quickplay.INSTANCE.settings.gameLogoScale;
    /**
     * The X position of the first column of game elements
     */
    int columnZeroX;
    /**
     * The scale of display strings for games
     */
    double stringScale = 1.0;
    /**
     * The number of columns of games
     */
    int columnCount = 1;
    /**
     * The column currently being calculated by {@link #initGui()}
     */
    int currentColumn = 0;
    /**
     * The row currently being calculated by {@link #initGui()}
     */
    int currentRow = 0;
    /**
     * The string displayed in the context menu to bind a game to a key
     */
    String favoriteString;

    // Used for button positions
    /**
     * Percentage padding between the sides of the window and the columns
     */
    public int windowPadding;
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
     * Whether this GUI should be rendered in compact mode or not
     * @see co.bugg.quickplay.config.ConfigSettings#compactMainMenu
     */
    boolean compact = Quickplay.INSTANCE.settings.compactMainMenu;

    /**
     * Constructor
     *
     * @param screen Screen this GUI is for
     */
    public QuickplayGuiScreen(Screen screen) {
        if(screen != null) {
            this.screen = screen;
        } else {
            throw new IllegalArgumentException("screen cannot be null.");
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        // Reset column/row number used for determining button positions
        this.currentColumn = 0;
        this.currentRow = 0;

        this.copyright = Quickplay.INSTANCE.translator.get("quickplay.gui.copyright", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        // Change the window Y padding if it's set
        // TODO test this
        if(Quickplay.INSTANCE.settings != null && Quickplay.INSTANCE.settings.mainMenuYPadding > 0) {
            this.scrollContentMargins = Quickplay.INSTANCE.settings.mainMenuYPadding;
        }

        // Strings aren't rendered in compact mode so forget about this
        if(!this.compact) {
            if (screen.buttonKeys.length > 0) {
                // Calculate the average width of all strings & what the longest one is
                for (final String buttonKey : this.screen.buttonKeys) {
                    final Button button = Quickplay.INSTANCE.buttonMap.get(buttonKey);
                    // TODO check restrictions
                    final int stringWidth = this.fontRendererObj.getStringWidth(Quickplay.INSTANCE.translator.get(button.translationKey));
                    if (stringWidth > this.longestStringWidth) {
                        this.longestStringWidth = stringWidth;
                    }
                }
            }

            // String scales up with size of game images
            if (this.gameImgSize * this.scaleMultiplier > 100) {
                stringScale = 2.0;
            } else if (this.gameImgSize * this.scaleMultiplier > 50) {
                this.stringScale = 1.5;
            } else {
                this.stringScale = 1.0;
            }
        } else {
            this.stringScale = 0;
        }

        // If logo exists, add padding for that logo
        if(this.screen.imageURL != null && this.screen.imageURL.length() > 0) {
            this.scrollContentMargins += this.gameImgSize / 2;
        }
        if(this.screen.translationKey != null && this.screen.translationKey.length() > 0) {
            this.scrollContentMargins += this.fontRendererObj.FONT_HEIGHT * this.stringScale + 10;
        }

        final int itemWidth = (int) ((this.gameImgSize * this.scaleMultiplier) +
                (this.longestStringWidth * this.stringScale) + this.stringLeftMargins + this.boxXMargins);

        // Calculate column count
        if(this.screen.screenType == ScreenType.BUTTONS) {
            windowPadding = (int) (width * (width > 500 ? 0.25 : 0.15));
            columnCount = (int) Math.floor((double) (width - windowPadding) / (buttonWidth + buttonMargins));
            if(this.columnCount <= 0) {
                this.columnCount = 1;
                buttonWidth = width - buttonMargins * 2;
            }
        } else {
            this.columnCount = (int) Math.floor((double) (this.width - this.windowXPadding) / itemWidth);
            if(this.columnCount <= 0) {
                this.columnCount = 1;
            }
        }
        if(columnCount > this.screen.buttonKeys.length) {
            columnCount = this.screen.buttonKeys.length;
        }

        // Calculate X location of the furthest left column (i.e. column zero)
        if(this.screen.screenType == ScreenType.BUTTONS) {
            this.columnZeroX = this.width / 2 - (this.buttonWidth + this.buttonMargins) * this.columnCount / 2;
        } else {
            this.columnZeroX = (this.width / 2 - this.columnCount * itemWidth / 2);
        }
        // Column zero can't be off the screen
        if(this.columnZeroX < 0) {
            this.columnZeroX = 0;
        }

        this.favoriteString = Quickplay.INSTANCE.translator.get("quickplay.gui.favorite");

        // Add buttons to the component list in the proper grid
        int nextButtonId = 0;
        for(String buttonKey : this.screen.buttonKeys) {
            final Button button = Quickplay.INSTANCE.buttonMap.get(buttonKey);
            // TODO check restrictions on button
            if(button == null) {
                continue;
            }

            if(this.screen.screenType == ScreenType.BUTTONS) {
                this.componentList.add(new QuickplayGuiButton(button, nextButtonId, columnZeroX + (buttonWidth + buttonMargins) * currentColumn,
                        scrollContentMargins / 2 + (buttonHeight + buttonMargins) * currentRow,
                        buttonWidth, buttonHeight, Quickplay.INSTANCE.translator.get(button.translationKey), true));
            } else {
                // Create invisible button                                                                                                                                                                                              // Width can't be affected by scaling                       // Texture is of the game icon, although it's not rendered (opacity is 0 in drawScreen)
                this.componentList.add(new QuickplayGuiButton(button, nextButtonId, this.columnZeroX + this.currentColumn * itemWidth,
                        (int) ((this.gameImgSize * this.scaleMultiplier + this.BoxYPadding) * this.currentRow + this.scrollContentMargins / 2),
                        (int) (itemWidth / this.scaleMultiplier), this.gameImgSize, "", new ResourceLocation(Reference.MOD_ID,
                        Hashing.md5().hashString(button.imageURL, StandardCharsets.UTF_8).toString() + ".png"),
                        0, 0, this.scaleMultiplier, true));
            }

            this.currentColumn++;
            // Goto next row if end of row
            if(this.currentColumn + 1 > this.columnCount) {
                this.currentColumn = 0;
                this.currentRow++;
            }
            nextButtonId++;
        }

        // Add back button if there are back button actions for this screen.
        if(this.screen.backButtonActions != null && this.screen.backButtonActions.length > 0) {
            this.componentList.add(new QuickplayGuiButton(null, nextButtonId, 3, 3, 100, 20,
                    Quickplay.INSTANCE.translator.get("quickplay.gui.back"), false));
        }

        setScrollingValues();
    }

    /**
     * Draw the screen for a screen type IMAGES.
     * @see ScreenType
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param partialTicks Partial tick completion
     * @param staticColor The QuickplayColor to use for text normally
     * @param hoverColor The QuickplayColor to use for text that the user is hovering over
     */
    private void drawImagesScreen(int mouseX, int mouseY, float partialTicks, QuickplayColor staticColor, QuickplayColor hoverColor) {

        // Draw images & strings for all the games buttons
        for(final QuickplayGuiComponent component : componentList) {
            if(component.origin instanceof Button) {
                final int scrollAdjustedY = component.scrollable ? component.y - scrollPixel : component.y;
                GlStateManager.color(1, 1, 1);
                final Button button = (Button) component.origin;
                // Draw icon
                GlStateManager.color(1, 1, 1, opacity);
                GlStateManager.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID,
                        Hashing.md5().hashString(button.imageURL,
                                StandardCharsets.UTF_8).toString() + ".png"));
                drawTexturedModalRect((int) (component.x / scaleMultiplier), (int) (scrollAdjustedY / scaleMultiplier),
                        0, 0, gameImgSize, gameImgSize);
                GlStateManager.scale(1 / scaleMultiplier, 1 / scaleMultiplier, 1 / scaleMultiplier);

                if(!compact && opacity > 0) {

                    // Draw text
                    GlStateManager.scale(stringScale, stringScale, stringScale);
                    final int color = component.mouseHovering(this, mouseX, mouseY) && contextMenu == null ?
                            hoverColor.getColor().getRGB() : staticColor.getColor().getRGB();
                    drawString(mc.fontRendererObj, Quickplay.INSTANCE.translator.get(button.translationKey),
                            (int) ((component.x + gameImgSize * scaleMultiplier + stringLeftMargins) / stringScale),
                            (int) ((((scrollAdjustedY + component.height / 2)) - fontRendererObj.FONT_HEIGHT / 2) / stringScale),
                            color & 0xFFFFFF | (int) (opacity * 255) << 24);
                    GlStateManager.scale(1 / stringScale, 1 / stringScale, 1 / stringScale);
                }
            }
        }

        GlStateManager.enableBlend();

        drawScrollbar(width - scrollbarWidth - 5);

        // OVERRIDE
        //super.drawScreen(mouseX, mouseY, partialTicks);
        for (QuickplayGuiComponent component : componentList) {
            if(component instanceof QuickplayGuiContextMenu || component.origin == null) {
                component.draw(this, mouseX, mouseY, opacity);
            }

            // If hovering & in compact mode, draw hover text
            if(compact && component.origin instanceof Button && component.mouseHovering(this, mouseX, mouseY)) {
                final Button button = (Button) component.origin;
                drawHoveringText(new ArrayList<>(Collections.singletonList(Quickplay.INSTANCE.translator.get(button.translationKey))),
                        mouseX, mouseY);
            }
        }

        if(opacity > 0) {
            drawCenteredString(fontRendererObj, copyright, width / 2,
                    height - fontRendererObj.FONT_HEIGHT - copyrightMargins,
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        }
    }

    /**
     * Draw the screen for a screen type BUTTONS.
     * @see ScreenType
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param partialTicks Partial tick completion
     */
    private void drawButtonsScreen(int mouseX, int mouseY, float partialTicks) {

        // Draw a background behind the buttons
        //final int bottomOfBox = (int) (Math.ceil((double) game.modes.size() / columnCount) * (buttonHeight + buttonMargins) - buttonMargins + scrollFadeLine + backgroundBoxPadding);
        //drawRect(columnZeroRowZeroX - backgroundBoxPadding, topOfBackgroundBox, rightOfBox, bottomOfBox, (int) (opacity * 255 * 0.5) << 24);

        // Modified super.drawScreen()
        for (QuickplayGuiComponent component : componentList) {
            if(opacity > 0) {
                component.draw(this, mouseX, mouseY, opacity);
            }
        }

        drawScrollbar(width - scrollbarWidth - 5);

        if(opacity > 0) {
            drawCenteredString(fontRendererObj, copyright, width / 2, height - fontRendererObj.FONT_HEIGHT - copyrightMargins,
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        this.drawDefaultBackground();
        this.updateOpacity();

        if(Quickplay.INSTANCE.isEnabled) {
            double mainLogoMultiplier = this.scaleMultiplier / 1.5;
            // Draw screen logo if it's set
            if(this.screen.imageURL != null && this.screen.imageURL.length() > 0) {
                GlStateManager.color(1, 1, 1, opacity);
                GlStateManager.scale(mainLogoMultiplier, mainLogoMultiplier, mainLogoMultiplier);
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID,
                        Hashing.md5().hashString(this.screen.imageURL,
                                StandardCharsets.UTF_8).toString() + ".png"));
                drawTexturedModalRect((int) ((this.width / 2 - this.gameImgSize / 2 * mainLogoMultiplier) / mainLogoMultiplier),
                        (int) ((20 - this.scrollPixel) / mainLogoMultiplier),
                        0, 0, this.gameImgSize, this.gameImgSize);
                GlStateManager.scale(1 / mainLogoMultiplier, 1 / mainLogoMultiplier, 1 / mainLogoMultiplier);

            }

            // Swap the static non-hover color and the hover color if the user has the setting enabled.
            QuickplayColor staticColor = Quickplay.INSTANCE.settings.secondaryColor;
            QuickplayColor hoverColor = Quickplay.INSTANCE.settings.primaryColor;
            if(Quickplay.INSTANCE.settings.swapMainGuiColors) {
                staticColor = Quickplay.INSTANCE.settings.primaryColor;
                hoverColor = Quickplay.INSTANCE.settings.secondaryColor;
            }

            // Draw screen name if it's set
            if(this.screen.translationKey != null && this.screen.translationKey.length() > 0) {
                GlStateManager.scale(stringScale, stringScale, stringScale);
                drawCenteredString(this.fontRendererObj, Quickplay.INSTANCE.translator.get(this.screen.translationKey),
                        (int) ((this.width / 2) / stringScale),
                        (int) ((30 + this.gameImgSize * mainLogoMultiplier  - this.scrollPixel) / stringScale),
                        hoverColor.getColor().getRGB());

                GlStateManager.scale(1 / stringScale, 1 / stringScale, 1 / stringScale);
            }

            if(this.screen.screenType == ScreenType.IMAGES) {
                this.drawImagesScreen(mouseX, mouseY, partialTicks, staticColor, hoverColor);
            } else {
                this.drawButtonsScreen(mouseX, mouseY, partialTicks);
            }
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(this.fontRendererObj,
                    Quickplay.INSTANCE.translator.get("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.width / 2, this.height / 2, 0xffffff);
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
    public void setScrollingValues() {
        // scrollContentMargins is combination of top and bottom margins.
        // We only want top, so cut in half and add 30 for bottom
        this.scrollContentMargins = this.scrollContentMargins / 2 + 30;
        // Calculate the height of the scrollable content
        scrollContentHeight = calcScrollHeight();
        // Increase scroll speed & amount
        scrollMultiplier = 5;
        scrollDelay = 1;
        // Top & bottom of thee scroll frame
        scrollFrameBottom = height;
        scrollFrameTop = 0;
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {

        super.componentClicked(component);
        if(component.origin instanceof Button && contextMenu == null) {
            final Button button = (Button) component.origin;
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.socket.sendAction(new ButtonPressedAction(button.key));
                } catch (ServerUnavailableException e) {
                    e.printStackTrace();
                }
            });

            for(final String actionKey : button.actionKeys) {
                final AliasedAction aa = Quickplay.INSTANCE.aliasedActionMap.get(actionKey);
                if(aa == null) {
                    // TODO handle error
                    System.out.println("aa == null");
                    return;
                }
                // TODO check protocol
                // TODO check availableOn
                // TODO check admin state
                if(aa.action != null) {
                    aa.action.run();
                }
            }

            // Send analytical data to Google
            if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                    Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("GUIs", "Main Menu Option Pressed")
                                .setEventLabel(((Game) component.origin).name)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else if(component.origin == null) {
            // The only component with a null origin is the back button, at the moment.
            for(int i = 0; i < this.screen.backButtonActions.length; i++) {
                final String actionKey = this.screen.backButtonActions[i];
                final AliasedAction action = Quickplay.INSTANCE.aliasedActionMap.get(actionKey);
                if(action == null) {
                    continue;
                }
                // TODO perform permission checks
                action.action.run();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(mouseButton != 1) {
            return;
        }
        for(QuickplayGuiComponent component : this.componentList) {
            if(component instanceof QuickplayGuiContextMenu) {
                continue;
            }
            if(!component.mouseHovering(this, mouseX, mouseY)) {
                continue;
            }

            this.contextMenu = new QuickplayGuiContextMenu(Collections.singletonList(this.favoriteString), component, -1, mouseX, mouseY) {
                @Override
                public void optionSelected(int index) {
                    closeContextMenu();
                    if (index == 0) {
                        if (component.origin instanceof Button) {    // Open key binding GUI & add new keybind
                            Button button = (Button) component.origin;
                            Quickplay.INSTANCE.keybinds.keybinds.add(new QuickplayKeybind(Keyboard.KEY_NONE, button.key));
                        }
                        try {
                            Quickplay.INSTANCE.keybinds.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Quickplay.INSTANCE.sendExceptionRequest(e);
                        }
                        Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());
                    }
                }
            };
            this.addComponent(contextMenu);
            break;
        }


    }
}
