package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.util.GsonPostProcessorFactory;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.io.Serializable;

/**
 * Quickplay's Keybind system
 * These Keybinds have two keybind optiosn:
 * <ul>
 *     <li>Chat commands</li>
 *     <li>GUI opening</li>
 * </ul>
 */
public class QuickplayKeybind implements Serializable, GsonPostProcessorFactory.PostProcessor{

    // NEW FIELDS:
    // key - trigger key
    // target - target button ID / the button that was right-clicked to create this keybind
    // TODO When rebinding keybinds, while checking if any other keybind uses the same key, before denying also
    //  check if the server lists at all overlap. If not, then the keybind should be allowed.
    //  The keybind changing GUI can also only display keybinds which match the current server,
    //  and contain a "show all" button.


    /**
     * Name of this keybind
     */
    public String name;
    /**
     * The key that triggers this keybind
     */
    public int key;

    /**
     * If this keybind runs a command:
     * The chat command this keybind triggers
     */
    public String chatCommand = null;
    /**
     * If this keybind opens a GUI:
     * The name of the class to the GUI
     */
    public String className = null;
    /**
     * If this keybind opens a GUI:
     * The parameters to the constructor for the GUI
     */
    public Object[] constructorParams = null;
    /**
     * Whether the user has to hold down the key for this keybind for it to activate.
     */
    public boolean requiresPressTimer = false;
    /**
     * The number of times this keybind is activated. This is used to make sure the user doesn't release the
     * keybind and repress it before the keybind hold duration is complete.
     */
    private transient int pressCount = 0;

    /**
     * Constructor
     *
     * @param name Name of the keybind
     * @param defaultKey Default key for this keybind
     * @param chatCommand Chat command to run when this keybind's key is pressed
     */
    public QuickplayKeybind(String name, int defaultKey, String chatCommand) {
        this(name, defaultKey);
        this.chatCommand = chatCommand;
        if(!this.chatCommand.startsWith("/")) {
            this.chatCommand = "/" + chatCommand;
        }
    }

    /**
     * Constructor
     *
     * @param name Name of the keybind
     * @param defaultKey Default key for this keybind
     * @param guiClass Class for the GUI that this keybind opens
     * @param guiConstructorParams Parameters for the GUI class constructor that this keybind opens
     */
    public QuickplayKeybind(String name, int defaultKey, Class<? extends GuiScreen> guiClass, Object... guiConstructorParams) {
        this(name, defaultKey);
        this.className = guiClass.getName();
        this.constructorParams = guiConstructorParams;
    }

    /**
     * Constructor
     *
     * @param name Name of this keybind
     * @param defaultKey Default key for this keybind
     */
    public QuickplayKeybind(String name, int defaultKey) {
        this.name = name;
        this.key = defaultKey;

        registerAsEventHandler();
    }

    /**
     * Register this keybind as an event handler
     */
    private void registerAsEventHandler() {
        Quickplay.INSTANCE.registerEventHandler(this);
    }

    /**
     * Called whenever this keybind is triggered
     */
    public void keyPressed() {
        // Open a GUI if one is available
        if(className != null && constructorParams != null) {
            try {
                final Class clazz = Class.forName(className);
                if (clazz == null || (clazz != GuiScreen.class && !GuiScreen.class.isAssignableFrom(clazz))) {
                    throw new IllegalArgumentException("class corresponding to className could not be found or is not of type GuiScreen");
                }

                Class[] paramsClasses = new Class[constructorParams.length];
                int i = 0;
                for (Object param : constructorParams)
                    paramsClasses[i++] = param.getClass();

                final GuiScreen screen = (GuiScreen) clazz.getDeclaredConstructor(paramsClasses)
                        .newInstance(constructorParams);
                // Minecraft doesn't like opening GUIs outside the main thread, or else the cursor disappears.
                // https://www.minecraftforge.net/forum/topic/36866-189mouse-not-showing-up-in-gui/
                QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                    Minecraft.getMinecraft().displayGuiScreen(screen);
                });

                // Send analytical data to Google
                if (Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                        Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                    Quickplay.INSTANCE.threadPool.submit(() -> {
                        try {
                            Quickplay.INSTANCE.ga.createEvent("Keybinds", "Keybind Pressed")
                                    .setEventLabel(clazz.getName() + " (" + StringUtils.join(constructorParams, ", ") + ")")
                                    // Event value 0 for GUI, event value 1 for command
                                    .setEventValue(0)
                                    .send();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.keybinds.illegal", name)
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
        }

        if(chatCommand != null) {
            if(!chatCommand.startsWith("/")) {
                chatCommand = "/" + chatCommand;
            }

            Quickplay.INSTANCE.chatBuffer.push(chatCommand);

            // Send analytical data to Google
            if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                    Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Keybinds", "Keybind Pressed")
                                .setEventLabel(chatCommand)
                                // Event value 0 for GUI, event value 1 for command
                                .setEventValue(1)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void postDeserializationProcess() {
        registerAsEventHandler();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        // Keybinds only work On Hypixel and if Quickplay is enabled.
        if(!Quickplay.INSTANCE.checkEnabledStatus() || !Quickplay.INSTANCE.onHypixel) {
            return;
        }
        if(key != Keyboard.KEY_NONE && Keyboard.isKeyDown(key)) {
            int currentPressCount = ++this.pressCount;

            Quickplay.INSTANCE.threadPool.submit(() -> {
                if(this.requiresPressTimer) {
                    try {
                        Thread.sleep((long) (Quickplay.INSTANCE.settings.keybindPressTime * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /* Make sure the key wasn't let go and pressed again between
                 the original press and the time this code runs. */
                if(Keyboard.isKeyDown(key) && this.pressCount == currentPressCount) {
                    keyPressed();
                }
            });
        }
    }
}
