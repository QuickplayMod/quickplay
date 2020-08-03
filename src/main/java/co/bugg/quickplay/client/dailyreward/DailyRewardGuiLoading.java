package co.bugg.quickplay.client.dailyreward;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import org.lwjgl.opengl.GL11;

/**
 * GUI displayed while daily reward is loading
 */
public class DailyRewardGuiLoading extends QuickplayGui {

    double loadingScale = 1.5;
    double subScale = 1.0;

    @Override
    public void initGui() {
        super.initGui();
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();
        updateOpacity();

        // "Loading..."
        GL11.glScaled(loadingScale, loadingScale, loadingScale);
        drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.loading"),
                (int) (width / 2 / loadingScale), (int) ((height / 2 - fontRendererObj.FONT_HEIGHT) / loadingScale) - 5,
                0xFFFFFFFF);
        GL11.glScaled(1 / loadingScale, 1 / loadingScale, 1 / loadingScale);

        // "Please allow up to 5 seconds..."
        GL11.glScaled(subScale, subScale, subScale);
        drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.loading.sub"),
                (int) (width / 2 / subScale), (int) ((height / 2) / subScale) + 5, 0xFFFFFFFF);
        GL11.glScaled(1 / subScale, 1 / subScale, 1 / subScale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
