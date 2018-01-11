package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.GsonPostProcessorFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.Serializable;

public abstract class QuickplayKeybind implements Serializable, GsonPostProcessorFactory.PostProcessor{

    public String name;
    public int key;

    public QuickplayKeybind(String name, int defaultKey) {
        this.name = name;
        this.key = defaultKey;

        registerAsEventHandler();
    }

    private void registerAsEventHandler() {
        Quickplay.INSTANCE.registerEventHandler(this);
    }

    public abstract void keyPressed();

    @Override
    public void postDeserializationProcess() {
        registerAsEventHandler();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if(Keyboard.isKeyDown(key))
            keyPressed();
    }
}
