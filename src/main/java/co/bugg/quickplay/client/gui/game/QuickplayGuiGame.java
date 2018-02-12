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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
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
    public final int scrollMargins = 5;
    public int scrollFadeLine = 0;
    public final int scrollbarWidth = 3;
    public final int scrollbarMargins = 3;

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

        windowPadding = (int) (width * (width > 500 ? 0.25 : 0.15));
        headerHeight = (int) (height * 0.05);
        // Responsive size
        headerScale = height > 300 ? 1.5 : 1.0;
        logoScale = height > 300 ? 0.25 : 0.15;

        topOfBackgroundBox = (int) (headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins + logoSize * logoScale) + logoBottomMargins;
        scrollFadeLine = topOfBackgroundBox + backgroundBoxPadding;

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
            componentList.add(new QuickplayGuiButton(next, index, columnZeroX + (buttonWidth + buttonMargins) * currentColumn, scrollFadeLine + (buttonHeight + buttonMargins) * currentRow, buttonWidth, buttonHeight, next.name));
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
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof Mode && contextMenu == null) {
            final Mode mode = (Mode) component.origin;
            // For security purposes, only actual commands are sent and chat messages can't be sent.
            if(mode.command.startsWith("/"))
                Quickplay.INSTANCE.chatBuffer.push(((Mode) component.origin).command);
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
        final int scrollFadeDistance = (scrollFadeLine - topOfBackgroundBox);
        for (QuickplayGuiComponent component : componentList) {
            double scrollOpacity = (component.y > scrollFadeLine ? 1 : component.y + scrollFadeDistance < scrollFadeLine ? 0 : (scrollFadeDistance - ((double) scrollFadeLine - (double) component.y)) / (double) scrollFadeDistance);
            component.opacity = scrollOpacity;
            if(opacity * scrollOpacity > 0)
                component.draw(this.mc, mouseX, mouseY, opacity * scrollOpacity);
        }

        // Draw scrollbar if the top & bottom element aren't on the screen at the same time (Uses basically the same crappy code in QuickplayGuiEditConfig)
        if(componentList.get(0).y < scrollFadeLine || componentList.get(componentList.size() - 1).y + componentList.get(componentList.size() - 1).height > height) {
            // If context menu is opened, it'll affect the total component count but doesn't affect scrolling.
            final int elementCount = contextMenu == null ? componentList.size() : componentList.size() - 1;
            drawRect(rightOfBox - scrollbarWidth - scrollbarMargins,
                    // Top = percentage of elements above screen multiplied by height of scrollbar region, e.g. 50% above screen means top of scrollbar 50% down
                    (int) (componentList.stream().filter(component -> component.y <= topOfBackgroundBox).count() / (double) elementCount * (double) (height - topOfBackgroundBox - scrollbarMargins) + topOfBackgroundBox + backgroundBoxPadding),
                    rightOfBox - scrollbarMargins,
                    // Bottom = percentage of elements below screen multiplied by height of scrollbar region subtracted from height of scrollbar region, e.g. 50% below screen means bottom of scrollbar 50% up
                    height - (int) (componentList.stream().filter(component -> component.y + component.height >= height).count() / (double) elementCount * (double) (height - topOfBackgroundBox - scrollbarMargins) + scrollbarMargins),
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

        // Scroll is animated, one pixel per 1ms
        Quickplay.INSTANCE.threadPool.submit(() -> {

            // Figure out which component is the highest on screen & which is lowest
            QuickplayGuiComponent lowestComponent = null;
            QuickplayGuiComponent highestComponent = null;
            for(QuickplayGuiComponent component : componentList) {
                if(lowestComponent == null || lowestComponent.y < component.y)
                    lowestComponent = component;
                if(highestComponent == null || highestComponent.y > component.y)
                    highestComponent = component;
            }

            if(componentList.size() > 0)
                // Quick scrolling is important in this GUI so scroll speed * distance increased
                for (int i = 0; i < Math.abs(distance * 3); i++) {

                    // Only allow scrolling if there is an element off screen
                    // If scrolling down & the last element is at all off the screen (plus the additional margins for aesthetic purposes)
                    if((distance < 0 && lowestComponent.y > height - buttonHeight - scrollMargins) ||
                            // OR if scrolling up & the top element is currently at all off of the screen
                            (distance > 0 && highestComponent.y < scrollFadeLine)) {

                        for (QuickplayGuiComponent component : componentList) {
                            component.move(distance < 0 ? -1 : 1);
                        }

                        try {
                            Thread.sleep(2);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(QuickplayGuiComponent component : componentList) {
            if (!(component instanceof QuickplayGuiContextMenu) && component.mouseHovering(mc, mouseX, mouseY) && mouseButton == 1) {
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(new String[]{new ChatComponentTranslation("quickplay.gui.favorite").getUnformattedText()}), component, -1, mouseX, mouseY) {
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
