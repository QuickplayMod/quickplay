package co.bugg.quickplay.client.gui.dailyreward;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;

/**
 * GUI displayed while daily reward is loading
 */
public class DailyRewardGuiLoading extends QuickplayGui {

    double loadingScale = 1.5;
    double subScale = 1.0;

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {

        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        this.drawDefaultBackground();
        this.updateOpacity();

        // "Loading..."
        GlStateManagerWrapper.scale(loadingScale);
        this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.loading"),
                (int) (this.getWidth() / 2 / this.loadingScale), (int) ((this.getHeight() / 2 - this.getFontHeight()) / this.loadingScale) - 5,
                0xFFFFFFFF);
        GlStateManagerWrapper.scale(1 / this.loadingScale);

        // "Please allow up to 5 seconds..."
        GlStateManagerWrapper.scale(this.subScale);
        this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.loading.sub"),
                (int) (this.getWidth() / 2 / this.subScale), (int) ((this.getHeight() / 2) / this.subScale) + 5, 0xFFFFFFFF);
        GlStateManagerWrapper.scale(1 / this.subScale);

        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }
}
