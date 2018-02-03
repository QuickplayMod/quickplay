package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.games.Game;
import com.google.common.hash.Hashing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.charset.Charset;

public class QuickplayGuiGame extends QuickplayGui {
    public final Game game;

    public int headerHeight = 0;
    public double headerScale = 1.5;
    public final int headerBottomMargins = 3;
    public double logoScale = 0.25;

    public QuickplayGuiGame(Game game) {
        if(game != null)
            this.game = game;
        else
            throw new IllegalArgumentException("game cannot be null.");
    }

    @Override
    public void initGui() {
        super.initGui();
        headerHeight = (int) (height * 0.05);
        // Responsive size
        headerScale = height > 300 ? 1.5 : 1.0;
        logoScale = height > 300 ? 0.25 : 0.15;

        // add buttons
        componentList.add(new QuickplayGuiButton(null, 0, 0, 0, 64, 64, "", new ResourceLocation(Reference.MOD_ID, "textures/icons.png"), 0, 0, 1.0));
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
        GL11.glColor3f(1, 1, 1);
        final int logoSize = 256;
        mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, Hashing.md5().hashString(game.imageURL.toString(), Charset.forName("UTF-8")).toString() + ".png"));
        drawTexturedModalRect((float) ((width / 2 - logoSize * logoScale / 2) / logoScale), (float) ((headerHeight + fontRendererObj.FONT_HEIGHT * headerScale + headerBottomMargins) / logoScale), 0, 0, logoSize, logoSize);
        GL11.glScaled(1 / logoScale, 1 / logoScale, 1 / logoScale);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {

    }
}
