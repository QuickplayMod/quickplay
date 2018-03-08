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
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;

public class QuickplayGuiMainMenu extends QuickplayGui {

    final int gameImgSize = 256;

    // Margins & padding for GUI elements
    final int BoxYPadding = Math.max((int) (10 * Quickplay.INSTANCE.settings.gameLogoScale), 10);
    final int stringLeftMargins = 15;
    final int boxYMargins = 5;
    final int boxXMargins = 10;
    final int windowXPadding = 20;

    String copyright;
    final int copyrightMargins = 3;

    int longestStringWidth = 0;
    int averageStringWidth = 0;
    final double baseScaleMultiplier = 0.25;
    final double scaleMultiplier = baseScaleMultiplier * Quickplay.INSTANCE.settings.gameLogoScale;
    int columnZeroX;
    double stringScale = 1.0;
    int columnCount = 1;
    int currentColumn = 0;
    int currentRow = 0;

    String favoriteString = "Bind to key...";

    @Override
    public void initGui() {
        super.initGui();

        // Reset column/row number used for determining button positions
        currentColumn = 0;
        currentRow = 0;

        copyright = new ChatComponentTranslation("quickplay.gui.copyright", Calendar.getInstance().get(Calendar.YEAR)).getUnformattedText();

        // Change the window Y padding if it's set
        if(Quickplay.INSTANCE.settings != null && Quickplay.INSTANCE.settings.mainMenuYPadding > 0)
            scrollContentMargins = Quickplay.INSTANCE.settings.mainMenuYPadding;

        if(Quickplay.INSTANCE.gameList.size() > 0) {
            // Calculate the average width of all strings & what the longest one is
            for (Game game : Quickplay.INSTANCE.gameList) {
                final int stringWidth = fontRendererObj.getStringWidth(game.name);
                averageStringWidth += stringWidth;
                if (stringWidth > longestStringWidth) longestStringWidth = stringWidth;
            }
            averageStringWidth /= Quickplay.INSTANCE.gameList.size();
        }

        // String scales up with size of game images
        if(gameImgSize * scaleMultiplier > 100) {
            stringScale = 2.0;
        } else if(gameImgSize * scaleMultiplier > 50) {
            stringScale = 1.5;
        } else {
            stringScale = 1.0;
        }

        final int itemWidth = (int) (gameImgSize * scaleMultiplier + longestStringWidth * stringScale + stringLeftMargins + boxXMargins);
        // Calculate column count
        columnCount = (int) Math.floor((double) (width - windowXPadding) / itemWidth);
        if(columnCount <= 0) columnCount = 1;

        // Calculate X location of the furthest left column (i.e. column zero)
        columnZeroX = (width / 2 - columnCount * itemWidth / 2);
        // Column zero can't be off the screen
        if(columnZeroX < 0) columnZeroX = 0;

        favoriteString = new ChatComponentTranslation("quickplay.gui.favorite").getUnformattedText();

        // Add buttons to the component list in the proper grid
        int nextButtonId = 0;
        for(Game game : Quickplay.INSTANCE.gameList) {
            // Create invisible button                                                                                                                                                                                              // Width can't be affected by scaling                       // Texture is of the game icon, although it's not rendered (opacity is 0 in drawScreen)
            componentList.add(new QuickplayGuiButton(game, nextButtonId, columnZeroX + currentColumn * itemWidth, (int) ((gameImgSize * scaleMultiplier + BoxYPadding + boxYMargins * 2) * currentRow + scrollContentMargins / 2), (int) (itemWidth / scaleMultiplier), gameImgSize, "", new ResourceLocation(Reference.MOD_ID, Hashing.md5().hashString(game.imageURL.toString(), Charset.forName("UTF-8")).toString() + ".png"), 0, 0, scaleMultiplier, true));
            currentColumn++;
            if(currentColumn + 1 > columnCount) {
                currentColumn = 0;
                currentRow++;
            }
            nextButtonId++;
        }

        setScrollingValues();
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();

        // Increase scroll speed & amount
        scrollMultiplier = 5;
        scrollDelay = 1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        // if there are no games to display
        if(Quickplay.INSTANCE.gameList == null || Quickplay.INSTANCE.gameList.size() <= 0) {
            drawNoGamesMenu();
        } else {

            // Draw images & strings for all the games buttons
            for(QuickplayGuiComponent component : componentList) {
                final int scrollAdjustedY = component.scrollable ? component.y - scrollPixel : component.y;
                GL11.glColor3f(1, 1, 1);
                if(component.origin instanceof Game) {
                    // Draw icon
                    GL11.glColor4f(1, 1, 1, opacity);
                    GL11.glScaled(scaleMultiplier, scaleMultiplier, scaleMultiplier);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, Hashing.md5().hashString(((Game) component.origin).imageURL.toString(), Charset.forName("UTF-8")).toString() + ".png"));
                    drawTexturedModalRect((int) (component.x / scaleMultiplier), (int) (scrollAdjustedY / scaleMultiplier), 0, 0, gameImgSize, gameImgSize);
                    GL11.glScaled(1 / scaleMultiplier, 1 / scaleMultiplier, 1 / scaleMultiplier);

                    // Draw text
                    GL11.glScaled(stringScale, stringScale, stringScale);
                    final int color = component.mouseHovering(this, mouseX, mouseY) && contextMenu == null ? Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() : Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB();
                    drawString(mc.fontRendererObj, ((Game) component.origin).name, (int) ((component.x + gameImgSize * scaleMultiplier + stringLeftMargins) / stringScale), (int) ((((scrollAdjustedY + component.height / 2)) - fontRendererObj.FONT_HEIGHT / 2) / stringScale), color & 0xFFFFFF | (int) (opacity * 255) << 24);
                    GL11.glScaled(1 / stringScale, 1 / stringScale, 1 / stringScale);
                }
            }

            GL11.glEnable(GL11.GL_BLEND);

            drawScrollbar(width - scrollbarWidth - 5);

        }

        // OVERRIDE
        //super.drawScreen(mouseX, mouseY, partialTicks);
        for (QuickplayGuiComponent component : componentList) {
            component.draw(this, mouseX, mouseY, (component instanceof QuickplayGuiContextMenu) ? opacity : 0);
        }

        drawCenteredString(fontRendererObj, copyright, width / 2, height - fontRendererObj.FONT_HEIGHT - copyrightMargins, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    protected void drawNoGamesMenu() {

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        final int stringMargins = 7;
        final int boxMargins = 15;


        // Get the various strings displayed on screen
        final String lineOne = new ChatComponentTranslation("quickplay.gui.main.nogames.issue").getUnformattedText();
        final String lineTwo = new ChatComponentTranslation("quickplay.gui.main.nogames.why").getUnformattedText();
        final String lineThree = new ChatComponentTranslation("quickplay.gui.main.nogames.contact").getUnformattedText();

        // Calculate longest string for scaling
        int longestStringLength = mc.fontRendererObj.getStringWidth(lineOne) + boxMargins * 2;
        if(longestStringLength < mc.fontRendererObj.getStringWidth(lineTwo) + boxMargins * 2) longestStringLength = mc.fontRendererObj.getStringWidth(lineTwo) + boxMargins * 2;
        if(longestStringLength < mc.fontRendererObj.getStringWidth(lineThree) + boxMargins * 2) longestStringLength = mc.fontRendererObj.getStringWidth(lineThree) + boxMargins * 2;

        // Calculate scale and Y locations
        final int oopsHeaderY = (int) (height * 0.4);
        final double oopsHeaderScale = 2.0;
        final double errorScale = longestStringLength < width ? 1.0 : width / (double) (longestStringLength + stringMargins * 2);
        final int lineOneY = (int) (oopsHeaderY + mc.fontRendererObj.FONT_HEIGHT * oopsHeaderScale) + stringMargins;
        final int lineTwoY = (int) (lineOneY + mc.fontRendererObj.FONT_HEIGHT * errorScale) + stringMargins;
        final int lineThreeY = (int) (lineTwoY + mc.fontRendererObj.FONT_HEIGHT * errorScale) + stringMargins;

        // Draw background box
        drawRect(width / 2 - longestStringLength / 2 - boxMargins, oopsHeaderY - boxMargins, width / 2 + longestStringLength / 2 + boxMargins, (int) (lineThreeY + mc.fontRendererObj.FONT_HEIGHT * errorScale + boxMargins), (int) (opacity * 255 * 0.5) << 24);
        GL11.glEnable(GL11.GL_BLEND);

        // Draw header
        GL11.glScaled(oopsHeaderScale, oopsHeaderScale, oopsHeaderScale);
        drawCenteredString(mc.fontRendererObj, new ChatComponentTranslation("quickplay.gui.main.nogames.header").getUnformattedText(),
                (int) (width / 2 / oopsHeaderScale), (int) (oopsHeaderY / oopsHeaderScale), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        GL11.glScaled(1 / oopsHeaderScale, 1 / oopsHeaderScale, 1 / oopsHeaderScale);

        // Draw error text
        GL11.glScaled(errorScale, errorScale, errorScale);
        drawCenteredString(mc.fontRendererObj, new ChatComponentTranslation("quickplay.gui.main.nogames.issue").getUnformattedText(),
                (int) (width / 2 / errorScale), (int) (lineOneY / errorScale), Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        drawCenteredString(mc.fontRendererObj, new ChatComponentTranslation("quickplay.gui.main.nogames.why").getUnformattedText(),
                (int) (width / 2 / errorScale), (int) (lineTwoY / errorScale), Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        drawCenteredString(mc.fontRendererObj, new ChatComponentTranslation("quickplay.gui.main.nogames.contact").getUnformattedText(),
                (int) (width / 2 / errorScale), (int) (lineThreeY / errorScale), Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        GL11.glScaled(1 / errorScale, 1 / errorScale, 1 / errorScale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(QuickplayGuiComponent component : componentList) {
            if(!(component instanceof QuickplayGuiContextMenu) && component.mouseHovering(this, mouseX, mouseY) && mouseButton == 1) {
                //noinspection ArraysAsListWithZeroOrOneArgument
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(favoriteString), component, -1, mouseX, mouseY, false) {
                    @Override
                    public void optionSelected(int index) {
                        closeContextMenu();
                        switch(index) {
                            case 0:
                                if(component.origin instanceof Game)
                                    // Open key binding GUI & add new keybind
                                    Quickplay.INSTANCE.keybinds.keybinds.add(new QuickplayKeybind(((Game) component.origin).name, Keyboard.KEY_NONE, QuickplayGuiGame.class, ((Game) component.origin).unlocalizedName));

                                try {
                                    Quickplay.INSTANCE.keybinds.save();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Quickplay.INSTANCE.sendExceptionRequest(e);
                                }
                                Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());
                                break;

                            /*    // Priority management. Users can't modify priorities
                            case 1:
                            case 2:
                                // context menu origin contains a component. This component then has it's own origin,
                                // which, if the right-click is on a game icon, should be instanceof Game
                                final QuickplayGuiComponent origin = (QuickplayGuiComponent) this.origin;
                                if(origin.origin instanceof Game) {
                                    final Game game = (Game) origin.origin;
                                    final HashMap<String, Integer> gamePriorities = Quickplay.INSTANCE.settings.gamePriorities;
                                    // Default 0 priority
                                    if(!gamePriorities.containsKey(game.unlocalizedName)) {
                                        gamePriorities.put(game.unlocalizedName, 0);
                                    }

                                    // Get the current priority
                                    final int currentPriority = gamePriorities.get(game.unlocalizedName);

                                    // Raising priority
                                    if(index == 1) {
                                        // Get next highest priority
                                        int nextHighestPriority;
                                        Optional<Map.Entry<String, Integer>> nextHighestPriorityOptional = gamePriorities
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue() >= currentPriority && !entry.getKey().equals(game.unlocalizedName)).min(Comparator.comparing(Map.Entry::getValue));
                                        if(nextHighestPriorityOptional.isPresent())
                                            nextHighestPriority = nextHighestPriorityOptional.get().getValue();
                                        else break;

                                        // Set current priority to next highest priority + 1
                                        gamePriorities.put(game.unlocalizedName, ++nextHighestPriority);
                                    // Lowering priority
                                    } else {
                                        // Get next highest priority
                                        int nextLowestPriority;
                                        Optional<Map.Entry<String, Integer>> nextLowestPriorityOptional = gamePriorities
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue() <= currentPriority && !entry.getKey().equals(game.unlocalizedName)).max(Comparator.comparing(Map.Entry::getValue));
                                        if(nextLowestPriorityOptional.isPresent())
                                            nextLowestPriority = nextLowestPriorityOptional.get().getValue();
                                        else break;

                                        // Set current priority to next highest priority + 1
                                        gamePriorities.put(game.unlocalizedName, --nextLowestPriority);
                                    }

                                    Quickplay.INSTANCE.gameList = Arrays.asList(Quickplay.organizeGameList(Quickplay.INSTANCE.gameList.toArray(new Game[]{})));
                                    initGui();
                                    try {
                                        Quickplay.INSTANCE.settings.save();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            */
                        }
                    }
                };
                componentList.add(contextMenu);
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof Game && contextMenu == null) {
            mc.displayGuiScreen(new QuickplayGuiGame((Game) component.origin));
        }
    }
}
