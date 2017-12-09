package co.bugg.quickplay;

import co.bugg.quickplay.command.CommandQuickplay;
import co.bugg.quickplay.http.HttpRequestFactory;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.ResponseAction;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.MessageBuffer;
import co.bugg.quickplay.util.ReflectionUtil;
import co.bugg.quickplay.util.ServerChecker;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.8, 1.12.2]"
)
public class Quickplay {

    @Mod.Instance
    public static Quickplay INSTANCE = new Quickplay();

    /**
     * Whether the client is currently connected to the Hypixel network
     */
    public boolean onHypixel = false;
    /**
     * Whether the mod is currently enabled
     */
    public boolean enabled = false;
    /**
     * The reason the mod has been disabled, if it is disabled
     */
    public String disabledReason = null;
    /**
     * Verification method used to verify the client is online Hypixel
     */
    public ServerChecker.VerificationMethod verificationMethod;
    /**
     * Thread pool for blocking code
     */
    public final ExecutorService threadPool = Executors.newCachedThreadPool();
    /**
     * A list of all registered event handlers
     */
    public final List<Object> eventHandlers = new ArrayList<>();
    /**
     * A list of all registered commands
     */
    public final List<ICommand> commands = new ArrayList<>();
    /**
     * Buffer for sending messages to the client
     */
    public MessageBuffer messageBuffer;

    public HttpRequestFactory requestFactory;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // The message buffer should remain online even
        // if the mod is disabled - this allows for
        // communicating important information about why
        // the mod is currently disabled, or how to fix.
        messageBuffer = new MessageBuffer();
        messageBuffer.run();

        enable();
    }

    /**
     * Register a specific object as an event handler
     * @param handler Object to register
     */
    public void registerEventHandler(Object handler) {
        eventHandlers.add(handler);
        MinecraftForge.EVENT_BUS.register(handler);
    }

    /**
     * Unregister a specific object as an event handler
     * @param handler Object to unregister
     */
    public void unregisterEventHandler(Object handler) {
        eventHandlers.remove(handler);
        MinecraftForge.EVENT_BUS.unregister(handler);
    }

    /**
     * Enable the mod
     */
    public void enable() {
        if(!this.enabled) {
            this.enabled = true;
            requestFactory = new HttpRequestFactory();

            this.threadPool.submit(() -> {
                HashMap<String, String> params = new HashMap<>();
                requestFactory.addDebuggingParameters(params);

                Request request = requestFactory.newEnableRequest(params);
                WebResponse response = request.execute();

                for(ResponseAction action : response.actions) {
                    action.run();
                }
            });

            registerEventHandler(new QuickplayEventHandler());

            commands.add(new CommandQuickplay());
            commands.forEach(ClientCommandHandler.instance::registerCommand);
        }
    }

    /**
     * Disable the mod
     */
    public void disable(String reason) {
        if(this.enabled) {
            this.enabled = false;
            eventHandlers.forEach(this::unregisterEventHandler);
            this.disabledReason = reason;
        }
    }

    /**
     * Check if the mod is enabled, and
     * send a disabled message if not.
     * @return Whether the mod is enabled
     */
    public boolean checkEnabledStatus() {
        if(!enabled) {
            IChatComponent message = new ChatComponentTranslation("quickplay.disabled", this.disabledReason);
            message.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));

        }

        return enabled;
    }
}
