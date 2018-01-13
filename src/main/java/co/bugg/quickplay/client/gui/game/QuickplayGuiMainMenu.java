package co.bugg.quickplay.client.gui.game;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.GL11;

public class QuickplayGuiMainMenu extends QuickplayGui {

    int distance = 0;

    @Override
    public void initGui() {
        super.initGui();
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

        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void mouseScrolled(int distance) {
        this.distance += distance;

    }
}
