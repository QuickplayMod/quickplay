package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.util.Message;
import com.google.common.hash.Hashing;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.charset.Charset;

public class QuickplayGuiMainMenu extends QuickplayGui {

    final int gameImgSize = 256;

    // Margins & padding for GUI elements
    final int BoxYPadding = Math.max((int) (10 * Quickplay.INSTANCE.settings.gameLogoScale), 10);
    final int stringLeftMargins = 15;
    final int boxYMargins = 5;
    final int boxXMargins = 10;
    final int scrollMargins = 20;
    final int windowXPadding = 20;

    int longestStringWidth = 0;
    int averageStringWidth = 0;
    final double baseScaleMultiplier = 0.25;
    final double scaleMultiplier = baseScaleMultiplier * Quickplay.INSTANCE.settings.gameLogoScale;
    int columnZeroX;
    double stringScale = 1.0;
    int columnCount = 1;
    int currentColumn = 0;
    int currentRow = 0;

    @Override
    public void initGui() {
        super.initGui();
        // Reset column/row number used for determining button positions
        currentColumn = 0;
        currentRow = 0;

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

        // Add buttons to the component list in the proper grid
        int nextButtonId = 0;
        for(Game game : Quickplay.INSTANCE.gameList) {
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
        } else {
            super.drawScreen(mouseX, mouseY, partialTicks);

            // Draw strings for all the games buttons
            GL11.glScaled(stringScale, stringScale, stringScale);
            GL11.glEnable(GL11.GL_BLEND);
            for(QuickplayGuiComponent component : componentList) {
                if(component.origin instanceof Game) {
                    drawString(mc.fontRendererObj, ((Game) component.origin).name, (int) ((component.x + component.width + stringLeftMargins) / stringScale), (int) ((((component.y + component.height / 2)) - fontRendererObj.FONT_HEIGHT / 2) / stringScale), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                }
            }
            GL11.glScaled(1 / stringScale, 1 / stringScale, 1 / stringScale);
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
                    for (QuickplayGuiComponent component : componentList) {
                        component.move(distance < 0 ? -1 : 1);
                    }
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
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentText(((Game) component.origin).name)));
    }
}
