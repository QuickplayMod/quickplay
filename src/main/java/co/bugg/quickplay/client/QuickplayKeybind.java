package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.GsonPostProcessorFactory;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.Serializable;

public class QuickplayKeybind implements Serializable, GsonPostProcessorFactory.PostProcessor{

    public String name;
    public int key;

    public String chatCommand = null;

    public String className = null;
    public Object[] constructorParams = null;

    public QuickplayKeybind(String name, int defaultKey, String chatCommand) {
        this(name, defaultKey);
        this.chatCommand = "/" + chatCommand;
    }

    public QuickplayKeybind(String name, int defaultKey, Class<? extends GuiScreen> guiClass, Object... guiConstructorParams) {
        this(name, defaultKey);
        this.className = guiClass.getName();
        this.constructorParams = guiConstructorParams;
    }

    public QuickplayKeybind(String name, int defaultKey) {
        this.name = name;
        this.key = defaultKey;

        registerAsEventHandler();
    }

    private void registerAsEventHandler() {
        Quickplay.INSTANCE.registerEventHandler(this);
    }

    public void keyPressed() {
        // Open a GUI if one is available
        if(className != null && constructorParams != null)
            try {
                if(className != null) {
                    final Class clazz = Class.forName(className);
                    if(clazz == null || (clazz != GuiScreen.class && !GuiScreen.class.isAssignableFrom(clazz)))
                        throw new IllegalArgumentException("class corresponding to className could not be found or is not of type GuiScreen");

                    Class[] paramsClasses = new Class[constructorParams.length];
                    int i = 0;
                    for (Object param : constructorParams)
                        paramsClasses[i++] = param.getClass();

                    Minecraft.getMinecraft().displayGuiScreen((GuiScreen) clazz.getDeclaredConstructor(paramsClasses).newInstance(constructorParams));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.keybinds.illegal", name).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }

        if(chatCommand != null) {
            if(!chatCommand.startsWith("/"))
                chatCommand = "/" + chatCommand;

            Quickplay.INSTANCE.chatBuffer.push(chatCommand);
        }
    }

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
