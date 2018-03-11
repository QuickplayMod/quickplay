package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.QuickplayGuiContextMenu;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.Mode;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class QuickplayGuiGame extends QuickplayGui {
    public final Game game;

    public int headerHeight = 0;
    public double headerScale = 1.5;
    public final int headerBottomMargins = 3;
    public double logoScale = 0.25;
    public final int logoSize = 256;
    public final int logoBottomMargins = 10;
    public int topOfBackgroundBox = 0;
    public int backgroundBoxPadding = 10;

    // Used for button positions
    public int windowPadding = 0;
    public int columnCount = 1;
    public int columnZeroX = 0;
    public int currentColumn;
    public int currentRow;

    public final int buttonMargins = 5;
    public int buttonWidth = 200;
    public int buttonHeight = 20;

    public String copyright;
    public final int copyrightMargins = 3;

    public QuickplayGuiGame(Game game) {
        if(game != null)
            this.game = game;
        else
            throw new IllegalArgumentException("game cannot be null.");
    }

    /**
     * Used for keybinds, gameList is queried for the name of the game provided
     * @param unlocalizedGameName Name of the game to display if possible
     */
    @SuppressWarnings("unused")
    public QuickplayGuiGame(String unlocalizedGameName) {
        if(unlocalizedGameName != null) {
            List<Game> filteredList = Quickplay.INSTANCE.gameList.stream().filter(game -> game.unlocalizedName.equals(unlocalizedGameName)).collect(Collectors.toList());
            if(filteredList.size() <= 0)
                throw new IllegalArgumentException("unlocalizedGameName could not find a matching game in gameList!");
            else
                game = filteredList.get(0);
        } else
            throw new IllegalArgumentException("unlocalizedGameName cannot be null.");
    }

    @Override
    public void initGui() {
        super.initGui();
        currentColumn = currentRow = 0;

        copyright =  new ChatComponentTranslation("quickplay.gui.copyright", Calendar.getInstance().get(Calendar.YEAR)).getUnformattedText();

        windowPadding = (int) (width * (width > 500 ? 0.25 : 0.15));
        headerHeight = (int) (height * 0.05);
        // Responsive size
        headerScale = height > 300 ? 1.5 : 1.0;
        logoScale = height > 300 ? 0.25 : 0.15;

        topOfBackgroundBox = (int) (headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins + logoSize * logoScale) + logoBottomMargins;

        buttonWidth = 200;
        columnCount = (int) Math.floor((double) (width - windowPadding) / (buttonWidth + buttonMargins));
        // If no full size buttons can fit on screen, then set column count back to 1 & shrink buttons
        if(columnCount < 1) {
            columnCount = 1;
            buttonWidth = width - buttonMargins * 2;
        }
        // If there are more columns than items then decrease column count
        if(columnCount > game.modes.size())
            columnCount = game.modes.size();

        // Calculate X position of column zero
        columnZeroX = width / 2 - (buttonWidth + buttonMargins) * columnCount / 2;

        // add buttons
        for(ListIterator<Mode> iter = game.modes.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final Mode next = iter.next();
            componentList.add(new QuickplayGuiButton(next, index, columnZeroX + (buttonWidth + buttonMargins) * currentColumn, topOfBackgroundBox + backgroundBoxPadding + (buttonHeight + buttonMargins) * currentRow, buttonWidth, buttonHeight, next.name, true));
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
        componentList.add(new QuickplayGuiButton(null, game.modes.size() + 1, 3, 3, 100, 20, I18n.format("quickplay.gui.back"), false));

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
            if(mode.command.startsWith("/"))
                Quickplay.INSTANCE.chatBuffer.push(((Mode) component.origin).command);
        } else if(component.displayString.equals(I18n.format("quickplay.gui.back"))) {
            Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiMainMenu());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        GL11.glScaled(headerScale, headerScale, headerScale);
        drawCenteredString(fontRendererObj, game.name, (int) (width / 2 / headerScale), (int) (headerHeight / headerScale), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

        GL11.glScaled(logoScale, logoScale, logoScale);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, opacity);
        mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, Hashing.md5().hashString(game.imageURL.toString(), Charset.forName("UTF-8")).toString() + ".png"));
        drawTexturedModalRect((float) ((width / 2 - logoSize * logoScale / 2) / logoScale), (float) ((headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins) / logoScale), 0, 0, logoSize, logoSize);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glScaled(1 / logoScale, 1 / logoScale, 1 / logoScale);

        final int columnZeroRowZeroX = width / 2 - (buttonWidth + buttonMargins) * columnCount / 2;
        final int rightOfBox = columnZeroRowZeroX + columnCount * (buttonWidth + buttonMargins) - buttonMargins + backgroundBoxPadding;

        // Draw a background behind the buttons
        //final int bottomOfBox = (int) (Math.ceil((double) game.modes.size() / columnCount) * (buttonHeight + buttonMargins) - buttonMargins + scrollFadeLine + backgroundBoxPadding);
        //drawRect(columnZeroRowZeroX - backgroundBoxPadding, topOfBackgroundBox, rightOfBox, bottomOfBox, (int) (opacity * 255 * 0.5) << 24);

        // Modified super.drawScreen()
        final int scrollFadeDistance = (topOfBackgroundBox + backgroundBoxPadding - topOfBackgroundBox);
        for (QuickplayGuiComponent component : componentList) {
            double scrollOpacity = component.scrollable ? ((component.y - scrollPixel) > topOfBackgroundBox + backgroundBoxPadding ? 1 : (component.y - scrollPixel) + scrollFadeDistance < topOfBackgroundBox + backgroundBoxPadding ? 0 : (scrollFadeDistance - ((double) topOfBackgroundBox + backgroundBoxPadding - (double) (component.y - scrollPixel))) / (double) scrollFadeDistance) : 1;
            component.opacity = scrollOpacity;
            System.out.println(scrollOpacity);
            if(opacity * scrollOpacity > 0)
                component.draw(this, mouseX, mouseY, opacity * scrollOpacity);
        }

        drawScrollbar(rightOfBox);

        drawCenteredString(fontRendererObj, copyright, width / 2, height - fontRendererObj.FONT_HEIGHT - copyrightMargins, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(QuickplayGuiComponent component : componentList) {
            if (!(component instanceof QuickplayGuiContextMenu) && component.mouseHovering(this, mouseX, mouseY) && mouseButton == 1) {
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(new String[]{new ChatComponentTranslation("quickplay.gui.favorite").getUnformattedText()}), component, -1, mouseX, mouseY, false) {
                    @Override
                    public void optionSelected(int index) {
                        switch(index) {
                            case 0:
                                if(component.origin instanceof Mode)
                                    // Open key binding GUI & add new keybind
                                    Quickplay.INSTANCE.keybinds.keybinds.add(new QuickplayKeybind(game.name + " " + ((Mode) component.origin).name, Keyboard.KEY_NONE, ((Mode) component.origin).command));

                                try {
                                    Quickplay.INSTANCE.keybinds.save();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Quickplay.INSTANCE.sendExceptionRequest(e);
                                }
                                Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());
                                break;

                        }
                        closeContextMenu();
                    }
                };
                componentList.add(contextMenu);
                break;
            }
        }
    }
}
