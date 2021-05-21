package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.elements.Button;
import co.bugg.quickplay.util.GsonPostProcessorFactory;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

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
     * The key that triggers this keybind
     */
    public int key;
    /**
     * The Button ID that this keybind targets, i.e. the button that was right-clicked to create this keybind.
     */
    public String target;

    /**
     * Name of this keybind
     * @deprecated Use {@link this.target}'s translation
     */
    @Deprecated
    public String name;
    /**
     * If this keybind runs a command:
     * The chat command this keybind triggers
     * @deprecated Use {@link this.target}
     */
    @Deprecated
    public String chatCommand = null;
    /**
     * If this keybind opens a GUI:
     * The name of the class to the GUI
     * @deprecated Use {@link this.target}
     */
    @Deprecated
    public String className = null;
    /**
     * If this keybind opens a GUI:
     * The parameters to the constructor for the GUI
     * @deprecated Use {@link this.target}
     */
    @Deprecated
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
     * @deprecated Use QuickplayKeybind(int key, String target) instead.
     */
    @Deprecated
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
     * @deprecated Use QuickplayKeybind(int key, String target) instead.
     */
    @Deprecated
    public QuickplayKeybind(String name, int defaultKey, Class<? extends GuiScreen> guiClass, Object... guiConstructorParams) {
        this(name, defaultKey);
        this.className = guiClass.getName();
        this.constructorParams = guiConstructorParams;
    }

    /**
     * Constructor
     * @param key Key on the keyboard that triggers this keybind when pressed.
     * @param target Key of the target button to execute the actions of when this keybind is triggered.
     */
    public QuickplayKeybind(int key, String target) {
        this.key = key;
        this.target = target;
        registerAsEventHandler();
    }

    /**
     * Constructor
     *
     * @param name Name of this keybind
     * @param defaultKey Default key for this keybind
     * @deprecated Use QuickplayKeybind(int key, String target) instead.
     */
    @Deprecated
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
        if(!Quickplay.INSTANCE.checkEnabledStatus()) {
            return;
        }
        if(this.target == null || this.target.length() <= 0) {
            return;
        }
        final Button button = Quickplay.INSTANCE.elementController.getButton(this.target);
        if(button == null || button.actionKeys == null || button.actionKeys.length <= 0) {
            return;
        }
        if(!button.passesPermissionChecks()) {
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                    new QuickplayChatComponentTranslation("quickplay.keybindPressFail")
                            .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
                    , false, false));
            return;
        }
        button.run();
    }

    @Override
    public void postDeserializationProcess() {
        registerAsEventHandler();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
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
                    this.keyPressed();
                }
            });
        }
    }
}
