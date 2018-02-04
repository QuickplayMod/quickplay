package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.Mode;
import com.google.common.hash.Hashing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.charset.Charset;
import java.util.ListIterator;

public class QuickplayGuiGame extends QuickplayGui {
    public final Game game;

    public int headerHeight = 0;
    public double headerScale = 1.5;
    public final int headerBottomMargins = 3;
    public double logoScale = 0.25;
    public final int logoSize = 256;
    public final int logoBottomMargins = 5;

    // Used for button positions
    public int windowPadding = 0;
    public int columnCount = 1;
    public int columnZeroX = 0;
    public int currentColumn;
    public int currentRow;

    public final int buttonMargins = 5;
    public int buttonWidth = 200;
    public int buttonHeight = 20;

    public QuickplayGuiGame(Game game) {
        if(game != null)
            this.game = game;
        else
            throw new IllegalArgumentException("game cannot be null.");
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

        final int bottomOfLogo = (int) (headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins + logoSize * logoScale);

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
            componentList.add(new QuickplayGuiButton(next, index, columnZeroX + (buttonWidth + buttonMargins) * currentColumn, bottomOfLogo + logoBottomMargins + (buttonHeight + buttonMargins) * currentRow, buttonWidth, buttonHeight, next.name));
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
        if(component.origin instanceof Mode) {
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

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {
        // TODO
    }
}
