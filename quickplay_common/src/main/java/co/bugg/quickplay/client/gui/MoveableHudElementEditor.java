package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;

/**
 * Editor for Quickplay {@link MoveableHudElement}s
 */
public class MoveableHudElementEditor extends QuickplayGui {

    /**
     * Element being edited
     */
    private final MoveableHudElement element;

    /**
     * Ratio from the left of the screen this element is at
     */
    double xRatio;
    /**
     * Ratio from the top of the screen this element is at
     */
    double yRatio;

    /**
     * Constructor
     * @param element Element to edit
     */
    public MoveableHudElementEditor(MoveableHudElement element) {
        this.element = element;
        this.xRatio = element.getxRatio();
        this.yRatio = element.getyRatio();
    }

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        // Blend is enabled for the GUI fadein
        // Fade in opacity has to be applied individually to each component that you want to fade in
        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        if(Quickplay.INSTANCE.isEnabled) {
            this.drawDefaultBackground();
            this.element.render(this.xRatio, this.yRatio, this.opacity);
            super.hookRender(mouseX, mouseY, partialTicks);
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(Quickplay.INSTANCE.elementController
                            .translate("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.getWidth() / 2, this.getHeight() / 2, 0xffffff);
        }

        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }

    @Override
    public boolean hookMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.hookMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.moveTo(mouseX, mouseY);
        return false;
    }

    @Override
    public boolean hookMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.hookMouseClicked(mouseX, mouseY, mouseButton);
        this.moveTo(mouseX, mouseY);
        return false;
    }

    @Override
    public boolean hookMouseReleased(int mouseX, int mouseY, int state) {
        super.hookMouseReleased(mouseX, mouseY, state);
        this.save();
        return false;
    }

    @Override
    public boolean hookKeyTyped(char typedChar, int keyCode) {
        super.hookKeyTyped(typedChar, keyCode);

        // Move right 1 pixel
        if(keyCode == 205) {
            this.xRatio = ((this.xRatio * (double) this.element.screenWidth) + 1) / (double) this.element.screenWidth;
        // Move left 1 pixel
        } else if(keyCode == 203) {
            this.xRatio = ((this.xRatio * (double) this.element.screenWidth) - 1) / (double) this.element.screenWidth;
        // Move up 1 pixel
        } else if(keyCode == 200) {
            this.yRatio = ((this.yRatio * (double) this.element.screenHeight) - 1) / (double) this.element.screenHeight;
        // Move down 1 pixel
        } else if(keyCode == 208) {
            this.yRatio = ((this.yRatio * (double) this.element.screenHeight) + 1) / (double) this.element.screenHeight;
        }

        this.save();
        return false;
    }

    /**
     * Save the current position of the element being edited
     */
    public void save() {
        this.element.setxRatio(this.xRatio);
        this.element.setyRatio(this.yRatio);
        this.element.save();
    }

    /**
     * Move {@link #element} to specified mouseX and mouseY by calculating {@link #xRatio} and {@link #yRatio}
     * @param mouseX X position to move to
     * @param mouseY Y position to move to
     */
    public void moveTo(int mouseX, int mouseY) {
        this.xRatio = (double) mouseX / (double) this.element.screenWidth;
        this.yRatio = (double) mouseY / (double) this.element.screenHeight;
    }
}
