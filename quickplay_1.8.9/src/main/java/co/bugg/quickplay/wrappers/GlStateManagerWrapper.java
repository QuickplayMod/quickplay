package co.bugg.quickplay.wrappers;

import net.minecraft.client.renderer.GlStateManager;

public class GlStateManagerWrapper {

    public static void pushMatrix() {
        GlStateManager.pushMatrix();
    }

    public static void popMatrix() {
        GlStateManager.popMatrix();
    }

    public static void enableAlpha() {
        GlStateManager.enableAlpha();
    }

    public static void disableAlpha() {
        GlStateManager.disableAlpha();
    }

    public static void enableBlend() {
        GlStateManager.enableBlend();
    }

    public static void disableBlend() {
        GlStateManager.disableBlend();
    }

    public static void scale(double f) {
        GlStateManagerWrapper.scale(f, f, f);
    }

    public static void scale(double x, double y, double z) {
        GlStateManager.scale(x, y, z);
    }

    public static void color(float r, float g, float b) {
        GlStateManager.color(r, g, b);
    }
    public static void color(float r, float g, float b, float a) {
        GlStateManager.color(r, g, b, a);
    }
}
