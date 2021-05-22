package co.bugg.quickplay.wrappers;

import net.minecraft.util.ResourceLocation;

public class ResourceLocationWrapper {

    String domain;
    String key;

    public ResourceLocationWrapper(String key) {
        this.key = key;
    }
    public ResourceLocationWrapper(String domain, String key) {
        this.domain = domain;
        this.key = key;
    }

    public ResourceLocation get() {
        if(domain == null) {
            return new ResourceLocation(this.key);
        }
        return new ResourceLocation(this.domain, this.key);
    }
}
