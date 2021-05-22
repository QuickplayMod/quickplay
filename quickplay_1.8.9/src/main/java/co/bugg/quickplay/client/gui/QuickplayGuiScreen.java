package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.actions.serverbound.ButtonPressedAction;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiContextMenu;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.elements.AliasedAction;
import co.bugg.quickplay.elements.Button;
import co.bugg.quickplay.elements.Screen;
import co.bugg.quickplay.elements.ScreenType;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import co.bugg.quickplay.wrappers.ResourceLocationWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import com.google.common.hash.Hashing;
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
    public void hookInit() {
        super.hookInit();

        // Reset column/row number used for determining button positions
        this.currentColumn = 0;
        this.currentRow = 0;

        this.copyright = Quickplay.INSTANCE.elementController.translate("quickplay.gui.copyright",
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        // Change the window Y padding if it's set
        // TODO test this
        if(Quickplay.INSTANCE.settings != null && Quickplay.INSTANCE.settings.mainMenuYPadding > 0) {
            this.scrollContentMargins = Quickplay.INSTANCE.settings.mainMenuYPadding;
        }

        // Strings aren't rendered in compact mode so forget about this
        if(!this.compact) {
            if (this.screen.buttonKeys.length > 0) {
                // Calculate the average width of all strings & what the longest one is
                for (final String buttonKey : this.screen.buttonKeys) {
                    final Button button = Quickplay.INSTANCE.elementController.getButton(buttonKey);
                    // Skip buttons which won't be rendered in this context.
                    if(button == null || !button.passesPermissionChecks()) {
                        continue;
                    }
                    final int stringWidth = this.getStringWidth(Quickplay.INSTANCE.elementController.translate(button.translationKey));
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
            this.scrollContentMargins += this.getFontHeight() * this.stringScale + 10;
        }

        final int itemWidth = (int) ((this.gameImgSize * this.scaleMultiplier) +
                (this.longestStringWidth * this.stringScale) + this.stringLeftMargins + this.boxXMargins);

        // Calculate column count
        if(this.screen.screenType == ScreenType.BUTTONS) {
            this.windowPadding = (int) (this.getWidth() * (this.getWidth() > 500 ? 0.25 : 0.15));
            this.columnCount = (int) Math.floor((double) (this.getWidth() - this.windowPadding) / (this.buttonWidth + this.buttonMargins));
            if(this.columnCount <= 0) {
                this.columnCount = 1;
                this.buttonWidth = this.getWidth() - this.buttonMargins * 2;
            }
        } else {
            this.columnCount = (int) Math.floor((double) (this.getWidth() - this.windowXPadding) / itemWidth);
            if(this.columnCount <= 0) {
                this.columnCount = 1;
            }
        }
        if(this.columnCount > this.screen.buttonKeys.length) {
            this.columnCount = this.screen.buttonKeys.length;
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

        this.favoriteString = Quickplay.INSTANCE.elementController.translate("quickplay.gui.favorite");

        // Add buttons to the component list in the proper grid
        int nextButtonId = 0;
        for(final String buttonKey : this.screen.buttonKeys) {
            final Button button = Quickplay.INSTANCE.elementController.getButton(buttonKey);
            // Skip buttons which won't be rendered in this context.
            if(button == null || !button.passesPermissionChecks()) {
                continue;
            }

            if(this.screen.screenType == ScreenType.BUTTONS) {
                this.componentList.add(new QuickplayGuiButton(button, nextButtonId, this.columnZeroX +
                        (this.buttonWidth + this.buttonMargins) * this.currentColumn,
                        this.scrollContentMargins / 2 + (this.buttonHeight + this.buttonMargins) * this.currentRow,
                        this.buttonWidth, this.buttonHeight, Quickplay.INSTANCE.elementController.translate(button.translationKey), true));
            } else {
                // Create invisible button                                                                                                                                                                                              // Width can't be affected by scaling                       // Texture is of the game icon, although it's not rendered (opacity is 0 in drawScreen)
                this.componentList.add(new QuickplayGuiButton(button, nextButtonId, this.columnZeroX + this.currentColumn * itemWidth,
                        (int) ((this.gameImgSize * this.scaleMultiplier + this.BoxYPadding) * this.currentRow + this.scrollContentMargins / 2),
                        (int) (itemWidth / this.scaleMultiplier), this.gameImgSize, "", new ResourceLocationWrapper(Reference.MOD_ID,
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
                    Quickplay.INSTANCE.elementController.translate("quickplay.gui.back"), false));
        }

        this.setScrollingValues();
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
        for(final QuickplayGuiComponent component : this.componentList) {
            if(component.origin instanceof Button) {
                final int scrollAdjustedY = component.scrollable ? component.y - this.scrollPixel : component.y;
                GlStateManagerWrapper.color(1, 1, 1);
                final Button button = (Button) component.origin;
                // Draw icon
                GlStateManagerWrapper.color(1, 1, 1, this.opacity);
                GlStateManagerWrapper.scale(this.scaleMultiplier);
                Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID,
                        Hashing.md5().hashString(button.imageURL,
                                StandardCharsets.UTF_8).toString() + ".png"));
                this.drawTexturedModalRect((int) (component.x / this.scaleMultiplier), (int) (scrollAdjustedY / this.scaleMultiplier),
                        0, 0, this.gameImgSize, this.gameImgSize);
                GlStateManagerWrapper.scale(1 / this.scaleMultiplier);

                if(!this.compact && this.opacity > 0) {

                    // Draw text
                    GlStateManagerWrapper.scale(this.stringScale);
                    final int color = component.isMouseHovering(this, mouseX, mouseY) && this.contextMenu == null ?
                            hoverColor.getColor().getRGB() : staticColor.getColor().getRGB();
                    this.drawString(Quickplay.INSTANCE.elementController.translate(button.translationKey),
                            (int) ((component.x + this.gameImgSize * this.scaleMultiplier + this.stringLeftMargins) / this.stringScale),
                            (int) ((((scrollAdjustedY + component.height / 2)) - this.getFontHeight() / 2) / this.stringScale),
                            color & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    GlStateManagerWrapper.scale(1 / this.stringScale);
                }
            }
        }

        GlStateManagerWrapper.enableBlend();

        drawScrollbar(this.getWidth() - this.scrollbarWidth - 5);

        // OVERRIDE
        //super.hookRender(mouseX, mouseY, partialTicks);
        for (QuickplayGuiComponent component : this.componentList) {
            if(component instanceof QuickplayGuiContextMenu || component.origin == null) {
                component.draw(this, mouseX, mouseY, this.opacity);
            }

            // If hovering & in compact mode, draw hover text
            if(this.compact && component.origin instanceof Button && component.isMouseHovering(this, mouseX, mouseY)) {
                final Button button = (Button) component.origin;
                this.drawHoveringText(new ArrayList<>(Collections.singletonList(Quickplay.INSTANCE.elementController.translate(button.translationKey))),
                        mouseX, mouseY);
            }
        }

        if(this.opacity > 0) {
            this.drawCenteredString(this.copyright, this.getWidth() / 2, this.height - this.getFontHeight() - this.copyrightMargins,
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
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

        // Modified super.hookRender()
        for (final QuickplayGuiComponent component : this.componentList) {
            if(this.opacity > 0) {
                component.draw(this, mouseX, mouseY, this.opacity);
            }
        }

        this.drawScrollbar(this.getWidth() - this.scrollbarWidth - 5);

        if(this.opacity > 0) {
            this.drawCenteredString(this.copyright, this.getWidth() / 2, this.getHeight() - this.getFontHeight() - this.copyrightMargins,
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
        }
    }

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        this.drawDefaultBackground();
        this.updateOpacity();

        if(Quickplay.INSTANCE.isEnabled) {
            final double mainLogoMultiplier = this.scaleMultiplier / 1.5;
            // Draw screen logo if it's set
            if(this.screen.imageURL != null && this.screen.imageURL.length() > 0) {
                GlStateManagerWrapper.color(1, 1, 1, this.opacity);
                GlStateManagerWrapper.scale(mainLogoMultiplier);
                Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID,
                        Hashing.md5().hashString(this.screen.imageURL, StandardCharsets.UTF_8).toString() + ".png"));
                this.drawTexturedModalRect((int) ((this.width / 2 - this.gameImgSize / 2 * mainLogoMultiplier) / mainLogoMultiplier),
                        (int) ((20 - this.scrollPixel) / mainLogoMultiplier),
                        0, 0, this.gameImgSize, this.gameImgSize);
                GlStateManagerWrapper.scale(1 / mainLogoMultiplier);

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
                GlStateManagerWrapper.scale(this.stringScale);
                this.drawCenteredString(Quickplay.INSTANCE.elementController.translate(this.screen.translationKey),
                        (int) ((this.getWidth() / 2) / this.stringScale),
                        (int) ((30 + this.gameImgSize * mainLogoMultiplier  - this.scrollPixel) / this.stringScale),
                        hoverColor.getColor().getRGB());

                GlStateManagerWrapper.scale(1 / this.stringScale);
            }

            if(this.screen.screenType == ScreenType.IMAGES) {
                this.drawImagesScreen(mouseX, mouseY, partialTicks, staticColor, hoverColor);
            } else {
                this.drawButtonsScreen(mouseX, mouseY, partialTicks);
            }
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(Quickplay.INSTANCE.elementController
                    .translate("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.getWidth() / 2, this.getHeight() / 2, 0xffffff);
        }



        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }

    @Override
    public boolean hookKeyTyped(char typedChar, int keyCode) {
        super.hookKeyTyped(typedChar, keyCode);
        if(Quickplay.INSTANCE.settings.anyKeyClosesGui) {
            Quickplay.INSTANCE.minecraft.openGui(null);
        }
        return false;
    }

    @Override
    public void setScrollingValues() {
        // scrollContentMargins is combination of top and bottom margins.
        // We only want top, so cut in half and add 30 for bottom
        this.scrollContentMargins = this.scrollContentMargins / 2 + 30;
        // Calculate the height of the scrollable content
        this.scrollContentHeight = calcScrollHeight();
        // Increase scroll speed & amount
        this.scrollMultiplier = 5;
        this.scrollDelay = 1;
        // Top & bottom of thee scroll frame
        this.scrollFrameBottom = this.getHeight();
        this.scrollFrameTop = 0;
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {

        super.componentClicked(component);
        if(component.origin instanceof Button && this.contextMenu == null) {
            final Button button = (Button) component.origin;
            if(!button.passesPermissionChecks()) {
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                        new QuickplayChatComponentTranslation("quickplay.buttonPressFail")
                                .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
                        , false, false));
                return;
            }
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.socket.sendAction(new ButtonPressedAction(button.key));
                } catch (ServerUnavailableException e) {
                    e.printStackTrace();
                }
            });

            button.run();

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
                final AliasedAction action = Quickplay.INSTANCE.elementController.getAliasedAction(actionKey);
                if(action == null) {
                    System.out.println("WARN: Aliased action " + actionKey + " is not found.");
                    continue;
                }
                if(!action.passesPermissionChecks()) {
                    System.out.println("WARN: Aliased action " + actionKey + " does not pass permission checks.");
                    continue;
                }
                action.action.run();
            }
        }
    }

    @Override
    public boolean hookMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.hookMouseClicked(mouseX, mouseY, mouseButton);

        if(mouseButton != 1) {
            return false;
        }
        for(final QuickplayGuiComponent component : this.componentList) {
            if(component instanceof QuickplayGuiContextMenu) {
                continue;
            }
            if(!component.isMouseHovering(this, mouseX, mouseY)) {
                continue;
            }

            this.contextMenu = new QuickplayGuiContextMenu(Collections.singletonList(this.favoriteString), component, -1, mouseX, mouseY) {
                @Override
                public void optionSelected(int index) {
                    QuickplayGuiScreen.this.closeContextMenu();
                    if (index == 0) {
                        if (component.origin instanceof Button) {    // Open key binding GUI & add new keybind
                            final Button button = (Button) component.origin;
                            // FIXME not common
                            Quickplay.INSTANCE.keybinds.keybinds.add(new QuickplayKeybind(Keyboard.KEY_NONE, button.key));
                        }
                        try {
                            Quickplay.INSTANCE.keybinds.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Quickplay.INSTANCE.sendExceptionRequest(e);
                        }
                        Quickplay.INSTANCE.minecraft.openGui(new QuickplayGuiKeybinds());
                    }
                }
            };
            this.addComponent(contextMenu);
            break;
        }
        return false;
    }
}
