package co.bugg.quickplay.wrappers;

import net.minecraft.client.gui.ScaledResolution;

public class ScaledResolutionWrapper {

    ScaledResolution sr;
    public ScaledResolutionWrapper(MinecraftWrapper mc) {
        this.sr = new ScaledResolution(mc.mc);
    }

    public ScaledResolution get() {
        return this.sr;
    }

    public int getScaledWidth() {
        return this.sr.getScaledWidth();
    }
    public int getScaledHeight() {
        return this.sr.getScaledHeight();
    }
}
