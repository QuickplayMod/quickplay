package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.games.Game;
import com.google.common.hash.Hashing;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class QuickplayGuiMainMenu extends QuickplayGui {

    final int gameImgSize = 256;

    // Margins & padding for GUI elements
    final int BoxYPadding = Math.max((int) (10 * Quickplay.INSTANCE.settings.gameLogoScale), 10);
    final int stringLeftMargins = 15;
    final int boxYMargins = 5;
    final int boxXMargins = 10;
    final int scrollMargins = 20;
    final int windowXPadding = 20;
    final int scrollbarMargins = 10;

    int longestStringWidth = 0;
    int averageStringWidth = 0;
    final double baseScaleMultiplier = 0.25;
    final double scaleMultiplier = baseScaleMultiplier * Quickplay.INSTANCE.settings.gameLogoScale;
    final int scrollbarWidth = 3;
    int columnZeroX;
    double stringScale = 1.0;
    int columnCount = 1;
    int currentColumn = 0;
    int currentRow = 0;

    final int[] rightClickPos = new int[]{-1, -1};
    QuickplayGuiComponent rightClickedComponent = null;
    String favoriteString = "Bind to key...";
    int rightClickWidth = 100;
    int rightClickHeight = 30;
    final int rightClickMargins = 5;
    final int rightClickPadding = 4;
    final int rightClickStringYMargin = 3;

    @Override
    public void initGui() {
        super.initGui();
        // Reset column/row number used for determining button positions
        currentColumn = 0;
        currentRow = 0;

        // Close right-click menu
        rightClickedComponent = null;
        rightClickPos[0] = rightClickPos[1] = -1;

        // Calculate the average width of all strings & what the longest one is
        for(Game game : Quickplay.INSTANCE.gameList) {
            final int stringWidth = fontRendererObj.getStringWidth(game.name);
            averageStringWidth += stringWidth;
            if(stringWidth > longestStringWidth) longestStringWidth = stringWidth;
        }
        averageStringWidth /= Quickplay.INSTANCE.gameList.size();

        // String scales up with size of game images
        // TODO: Make string scale smaller if strings fall off screen
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

        // Calculate size and stuff for right click menu
        favoriteString = new ChatComponentTranslation("quickplay.gui.favorite").getUnformattedText();
        rightClickWidth = fontRendererObj.getStringWidth(favoriteString) + rightClickPadding * 2;
        rightClickHeight = fontRendererObj.FONT_HEIGHT + rightClickPadding * 2;

        // Add buttons to the component list in the proper grid
        int nextButtonId = 0;
        for(Game game : Quickplay.INSTANCE.gameList) {
            // TODO: Ideally buttons would extend out to be over the text as well, so you can click there and it still works. This might require a major rewrite.
            componentList.add(new QuickplayGuiButton(game, nextButtonId, columnZeroX + currentColumn * itemWidth, (int) ((gameImgSize * scaleMultiplier + BoxYPadding + boxYMargins * 2) * currentRow + scrollMargins), gameImgSize, gameImgSize, "", new ResourceLocation(Reference.MOD_ID, Hashing.md5().hashString(game.imageURL.toString(), Charset.forName("UTF-8")).toString() + ".png"), 0, 0, scaleMultiplier));
            currentColumn++;
            if(currentColumn + 1 > columnCount) {
                currentColumn = 0;
                currentRow++;
            }
            nextButtonId++;
        }
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
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glEnable(GL11.GL_BLEND);

            boolean rightClickMenuOpen = rightClickPos[0] >= 0 && rightClickPos[1] >= 0 && rightClickedComponent != null;

            // Draw strings for all the games buttons
            GL11.glScaled(stringScale, stringScale, stringScale);
            for(QuickplayGuiComponent component : componentList) {
                if(component.origin instanceof Game) {
                    final int color = component.mouseHovering(mc, mouseX, mouseY) && !rightClickMenuOpen ? Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() : Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB();
                    drawString(mc.fontRendererObj, ((Game) component.origin).name, (int) ((component.x + component.width + stringLeftMargins) / stringScale), (int) ((((component.y + component.height / 2)) - fontRendererObj.FONT_HEIGHT / 2) / stringScale), color & 0xFFFFFF | (int) (opacity * 255) << 24);
                }
            }
            GL11.glScaled(1 / stringScale, 1 / stringScale, 1 / stringScale);

            drawScrollBar();

            if(rightClickMenuOpen) {
                drawRightClickMenu(rightClickPos[0], rightClickPos[1], Arrays.asList(favoriteString, "Move..."), mouseX, mouseY);
            }
        }

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

    protected void drawScrollBar() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        // Draw scrollbar if the top & bottom element aren't on the screen at the same time (Uses basically the same crappy code in QuickplayGuiEditConfig)
        if(componentList.get(0).y < 0 || componentList.get(componentList.size() - 1).y + componentList.get(componentList.size() - 1).height > height)
            drawRect(width - scrollbarWidth - scrollbarMargins,
                    // Top = percentage of elements above screen multiplied by height of scrollbar region, e.g. 50% above screen means top of scrollbar 50% down
                    (int) (componentList.stream().filter(component -> component.y <= 0 && component.origin instanceof Game).count() / (double) componentList.size() * (double) (height - scrollbarMargins) + scrollbarMargins),
                    width - scrollbarMargins,
                    // Bottom = percentage of elements below screen multiplied by height of scrollbar region subtracted from height of scrollbar region, e.g. 50% below screen means bottom of scrollbar 50% up
                    height - (int) (componentList.stream().filter(component -> component.y + component.height >= height && component.origin instanceof Game).count() / (double) componentList.size() * (double) (height - scrollbarMargins) + scrollbarMargins),
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    protected void drawRightClickMenu(int x, int y, List<String> options, int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        // Calculate width & height
        String longestOption = "";
        for(String option : options)
            if(fontRendererObj.getStringWidth(longestOption) < fontRendererObj.getStringWidth(option))
                longestOption = option;

        final int width = fontRendererObj.getStringWidth(longestOption) + rightClickPadding * 2;                        // Top element doesn't have a Y margin
        final int height = (fontRendererObj.FONT_HEIGHT + rightClickStringYMargin) * options.size() + rightClickPadding * 2 - rightClickStringYMargin;

        // Draw right click box
        drawRect(x, y, x + width, y + height, (int) (opacity * 0.7 * 255) << 24);
        GL11.glEnable(GL11.GL_BLEND);

        for(ListIterator<String> iter = options.listIterator(); iter.hasNext();) {
            final int index = iter.nextIndex();
            final String string = iter.next();
            final int stringY = y + rightClickPadding + index * (fontRendererObj.FONT_HEIGHT + rightClickStringYMargin);
            drawString(fontRendererObj, string, x + rightClickPadding, stringY, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
            if(mouseX > x && mouseX < x + width && mouseY > stringY && mouseY < stringY + fontRendererObj.FONT_HEIGHT)
                drawRect(x + rightClickPadding, stringY + fontRendererObj.FONT_HEIGHT, x + rightClickPadding + fontRendererObj.getStringWidth(string), stringY + fontRendererObj.FONT_HEIGHT + 1, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

        // Scroll is animated, one pixel per 1ms
        Quickplay.INSTANCE.threadPool.submit(() -> {
            // Quick scrolling is important in this GUI so scroll speed * distance increased
            for (int i = 0; i < Math.abs(distance * 5); i++) {

                // Only allow scrolling if there is an element off screen
                // If scrolling down & the last element is at all off the screen (plus the additional margins for aesthetic purposes)
                if((distance < 0 && componentList.get(componentList.size() - 1).y > height - gameImgSize * scaleMultiplier - scrollMargins) ||
                        // OR if scrolling up & the top element is currently at all off of the screen
                        (distance > 0 && componentList.get(0).y < scrollMargins)) {

                    final int upOrDown = distance < 0 ? -1 : 1;
                    for (QuickplayGuiComponent component : componentList) {
                        component.move(upOrDown);
                    }
                    rightClickPos[1] += upOrDown;

                    try {
                        Thread.sleep(1);
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
        rightClickPos[0] = rightClickPos[1] = -1;
        rightClickedComponent = null;
        for(QuickplayGuiComponent component : componentList) {
            if(component.mouseHovering(mc, mouseX, mouseY) && mouseButton == 1) {
                rightClickPos[0] = mouseX;
                rightClickPos[1] = mouseY;
                rightClickedComponent = component;
                break;
            }
        }
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof Game && rightClickedComponent == null) {
            mc.displayGuiScreen(new QuickplayGuiGame((Game) component.origin));
            //Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentText(((Game) component.origin).name)));
        }
    }
}
