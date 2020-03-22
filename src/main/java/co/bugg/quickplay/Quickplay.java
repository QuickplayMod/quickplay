package co.bugg.quickplay;

import cc.hyperium.Hyperium;
import cc.hyperium.commands.BaseCommand;
import cc.hyperium.event.EventBus;
import cc.hyperium.event.InvokeEvent;
import cc.hyperium.event.client.InitializationEvent;
import cc.hyperium.event.client.PreInitializationEvent;
import cc.hyperium.internal.addons.IAddon;
import co.bugg.quickplay.client.command.CommandHub;
import co.bugg.quickplay.client.command.CommandQuickplay;
import co.bugg.quickplay.client.gui.InstanceDisplay;
import co.bugg.quickplay.client.gui.QuickplayGuiPartySpinner;
import co.bugg.quickplay.client.render.GlyphRenderer;
import co.bugg.quickplay.client.render.PlayerGlyph;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.config.AssetFactory;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.config.ConfigSettings;
import co.bugg.quickplay.config.ConfigUsageStats;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.PartyMode;
import co.bugg.quickplay.http.HttpRequestFactory;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.ResponseAction;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.InstanceWatcher;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.analytics.AnalyticsRequest;
import co.bugg.quickplay.util.analytics.GoogleAnalytics;
import co.bugg.quickplay.util.analytics.GoogleAnalyticsFactory;
import co.bugg.quickplay.util.buffer.ChatBuffer;
import co.bugg.quickplay.util.buffer.MessageBuffer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public class Quickplay implements IAddon {

    public static Quickplay INSTANCE;

    /**
     * Whether the mod is currently enabled
     */
    public boolean enabled = false;
    /**
     * The reason the mod has been disabled, if it is disabled
     */
    public String disabledReason = null;
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
    public final List<BaseCommand> commands = new ArrayList<>();
    /**
     * Buffer for sending messages to the client
     */
    public MessageBuffer messageBuffer;
    /**
     * Buffer for sending chat messages to the server
     */
    public ChatBuffer chatBuffer;
    /**
     * Factory for creating HTTP requests
     */
    public HttpRequestFactory requestFactory;
    /**
     * Factory for creating, loading, etc. of mod assets
     */
    public AssetFactory assetFactory;
    /**
     * List of games
     */
    public List<Game> gameList = new ArrayList<>();
    /**
     * InstanceWatcher that constantly watches for what Hypixel server instance the client is on
     */
    public InstanceWatcher instanceWatcher;
    /**
     * Display of the current Hypixel instance
     */
    public InstanceDisplay instanceDisplay;
    /**
     * Mods settings
     */
    public ConfigSettings settings;
    /**
     * Keybinds for the mod, and which buttons open what GUIs
     */
    public ConfigKeybinds keybinds;
    /**
     * Privacy settings for the mod's data collection
     */
    public ConfigUsageStats usageStats;
    /**
     * Whether the user should be asked if they want to share stats next time they join a server
     */
    public boolean promptUserForUsageStats = false;
    /**
     * Seconds between ping requests to the web server
     * 0 or less for no requests
     * If it becomes time for the mod to execute a request but
     * the frequency is equal to 0 or less, then all ping
     * requests will be cancelled until the mod re-enables (usually at restart).
     */
    public int pingFrequency = 0;
    /**
     * Thread containing code that's pinging the web server periodically
     */
    public Future pingThread;
    /**
     * Help menu for Quickplay Premium
     * retrieved from the <code>enable</code> endpoint on mod enable from the content field <code>premiumInfo</code>
     */
    public IChatComponent premiumAbout = null;
    /**
     * List of all player glyphs, which contains the URL to the glyph as well as the owner's UUID
     */
    public List<PlayerGlyph> glyphs = new ArrayList<>();
    /**
     * Quickplay's resource pack
     */
    public IResourcePack resourcePack;
    /**
     * Google Analytics API tracker
     */
    public GoogleAnalytics ga;
    /**
     * How many ping requests have been sent out
     */
    public int currentPing = 0;



    @Override
    public void onLoad() {
        registerEventHandler(this);
    }

    @Override
    public void onClose() {

    }

    @InvokeEvent
    public void preInit(PreInitializationEvent event) {
        requestFactory = new HttpRequestFactory();
        assetFactory = new AssetFactory();

        assetFactory.createDirectories();
        assetFactory.dumpOldCache();
        resourcePack = assetFactory.registerResourcePack();
    }

    @InvokeEvent
    public void init(InitializationEvent event) {
        INSTANCE = this;
        // The message buffer should remain online even
        // if the mod is disabled - this allows for
        // communicating important information about why
        // the mod is currently disabled, or how to fix.
        messageBuffer = (MessageBuffer) new MessageBuffer(100).start();
        enable();
    }

    /**
     * Register a specific object as an event handler
     * @param handler Object to register
     */
    public void registerEventHandler(Object handler) {
        if(!eventHandlers.contains(handler))
            eventHandlers.add(handler);
        EventBus.INSTANCE.register(handler);
    }

    /**
     * Unregister a specific object as an event handler
     * @param handler Object to unregister
     */
    public void unregisterEventHandler(Object handler) {
        eventHandlers.remove(handler);
        EventBus.INSTANCE.unregister(handler);
    }

    /**
     * Enable the mod
     */
    public void enable() {
        if(!this.enabled) {

            this.enabled = true;

            // Asset handling is moved to preInit due to Hyperium's workflow

            try {
                settings = (ConfigSettings) AConfiguration.load("settings.json", ConfigSettings.class);
                keybinds = (ConfigKeybinds) AConfiguration.load("keybinds.json", ConfigKeybinds.class);
                usageStats = (ConfigUsageStats) AConfiguration.load("privacy.json", ConfigUsageStats.class);
            } catch (IOException | JsonSyntaxException e) {
                // Config either doesn't exist or couldn't be parsed
                Hyperium.LOGGER.error(e.getMessage(), e);
                assetFactory.createDirectories();

                if(settings == null)
                    settings = new ConfigSettings();
                if(keybinds == null)
                    keybinds = new ConfigKeybinds(true);
                if(usageStats == null) {
                    promptUserForUsageStats = true;
                }

                try {
                    // Write the default config that we just made to save it
                    settings.save();
                    keybinds.save();
                } catch (IOException e1) {
                    // File couldn't be saved
                    Hyperium.LOGGER.error(e1.getMessage(), e1);
                    sendExceptionRequest(e1);
                    Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.config.saveerror").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                }
            }

            // Create new Google Analytics instance if possible
            if(usageStats != null && usageStats.statsToken != null) {
                createGoogleAnalytics();
            }

            // Send analytical data to Google
            if(usageStats != null && usageStats.statsToken != null && usageStats.sendUsageStats && ga != null) {
                threadPool.submit(() -> {
                    try {
                        ga.createEvent("Systematic Events", "Mod Enable")
                                .setSessionControl(AnalyticsRequest.SessionControl.START)
                                .send();
                    } catch (IOException e) {
                        Hyperium.LOGGER.error(e.getMessage(), e);
                    }
                });
            }

            // Try to load the previous game list from cache
            // Web server will probably instruct to reload if it's available
            try {
                final Game[] gameListArray = this.assetFactory.loadCachedGamelist();
                if(gameListArray != null)
                    this.gameList = java.util.Arrays.asList(gameListArray);
            } catch (Exception e) {
                Hyperium.LOGGER.error(e.getMessage(), e);
                sendExceptionRequest(e);
            }

            this.threadPool.submit(() -> {
                final Request request = requestFactory.newEnableRequest();
                if(request != null) {
                    final WebResponse response = request.execute();

                    if (response != null) {
                        for (ResponseAction action : response.actions) {
                            action.run();
                        }

                        try {
                            if (response.ok && response.content != null) {
                                // Add the premium about information
                                if(response.content.getAsJsonObject().get("premiumInfo") != null)
                                    premiumAbout = IChatComponent.Serializer.jsonToComponent(response.content.getAsJsonObject().get("premiumInfo").toString());
                                // Add all glyphs
                                if(response.content.getAsJsonObject().get("glyphs") != null)
                                    glyphs.addAll(Arrays.asList(new Gson().fromJson(response.content.getAsJsonObject().get("glyphs"), PlayerGlyph[].class)));
                            }
                        } catch (IllegalStateException e) {
                            Hyperium.LOGGER.error(e.getMessage(), e);
                            sendExceptionRequest(e);
                        }
                    }
                }
            });

            registerEventHandler(new GlyphRenderer());
            registerEventHandler(new QuickplayEventHandler());

            chatBuffer = (ChatBuffer) new ChatBuffer(100).start();
            instanceWatcher = new InstanceWatcher(30).start();
            instanceDisplay = new InstanceDisplay();

            commands.add(new CommandQuickplay());

            if(settings.redesignedLobbyCommand) {
                // Register lobby commands
                commands.add(new CommandHub("l"));
                commands.add(new CommandHub("lobby"));
                commands.add(new CommandHub("hub"));
                commands.add(new CommandHub("spawn"));
                commands.add(new CommandHub("leave"));
            }
            // Copy of the lobby command that doesn't override server commands
            // Used for "Go to Lobby" buttons
            commands.add(new CommandHub("quickplaylobby", "hub"));
            commands.forEach(Hyperium.INSTANCE.getHandlers().getHyperiumCommandHandler()::registerCommand);

            EventBus.INSTANCE.post(new QuickplayEnabledEvent(this));
        }
    }

    /**
     * Create the Google Analytics instance with customized settings for this Quickplay instance
     */
    public void createGoogleAnalytics() {
        ga = GoogleAnalyticsFactory.create(Reference.ANALYTICS_TRACKING_ID, usageStats.statsToken.toString(), Reference.MOD_NAME, Reference.VERSION);
        final AnalyticsRequest defaultRequest = ga.getDefaultRequest();

        defaultRequest.setLanguage(String.valueOf(Minecraft.getMinecraft().gameSettings.language).toLowerCase());

        final GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        defaultRequest.setScreenResolution(screen.getDisplayMode().getWidth() + "x" + screen.getDisplayMode().getHeight());

        // Determine User-Agent/OS
        // Example: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36
        final String systemDetails = System.getProperty("os.name") + " " + System.getProperty("os.version") + "; " + System.getProperty("os.arch");
        defaultRequest.setUserAgent("Mozilla/5.0 (" + systemDetails + ") " + Reference.MOD_NAME + " " + Reference.VERSION);
    }

    /**
     * Disable the mod
     */
    public void disable(String reason) {
        // TODO This gets stuck when event handlers are unregistered.
        if(this.enabled) {
            this.enabled = false;
            eventHandlers.forEach(this::unregisterEventHandler);
            this.disabledReason = reason;

            if(chatBuffer != null)
                chatBuffer.stop();

            if(instanceWatcher != null)
                instanceWatcher.stop();
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

    /**
     * Reorganizes a game list to obey priorities in {@link #settings}
     * @param gameList list of games to organize
     * @return organized list
     */
    public static Game[] organizeGameList(Game[] gameList) {
        List<Game> list = new ArrayList<>();
        Collections.addAll(list, gameList);
        list.sort(Comparator.comparing(game -> Quickplay.INSTANCE.settings.gamePriorities.getOrDefault(((Game) game).unlocalizedName, 0)).reversed());
        return list.toArray(new Game[0]);
    }

    /**
     * Send an exception request to Quickplay backend for error reporting
     * @param e Exception that occurred
     */
    public void sendExceptionRequest(Exception e) {
        if(usageStats != null && usageStats.sendUsageStats) {
            final WebResponse response = requestFactory.newExceptionRequest(e).execute();
            if(response != null)
                for(ResponseAction action : response.actions)
                    action.run();

            if(ga != null) {
                try {
                    ga.createException().setExceptionDescription(e.getMessage()).send();
                } catch (IOException e1) {
                    Hyperium.LOGGER.error(e1.getMessage(), e1);
                }
            }
        }
    }

    /**
     * Start a party mode session by randomizing selected games & then executing the command
     */
    public void launchPartyMode() {
        if(settings.partyModes.size() > 0) {
            if(settings.partyModeGui) {
                Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiPartySpinner());
            } else {
                // No GUI, handle randomization in chat

                // Send commencement message if delay is greater than 0 seconds
                if(settings.partyModeDelay > 0) {
                    messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.party.commencing").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE))));
                }

                // Calculate mode
                PartyMode mode;
                // Don't need to randomize on size 1
                if(settings.partyModes.size() == 1) {
                    mode = settings.partyModes.get(0);
                } else {
                    final Random random = new Random();
                    mode = settings.partyModes.get(random.nextInt(settings.partyModes.size()));
                }

                try {
                    Thread.sleep((long) (settings.partyModeDelay * 1000));
                } catch (InterruptedException e) {
                    Hyperium.LOGGER.error(e.getMessage(), e);
                }

                messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.party.sendingYou", mode.name).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))));
                chatBuffer.push(mode.command);
            }
        } else {
            messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.party.nogames").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    /**
     * Reload the Quickplay Resource pack that contains glyphs, icons, lang, etc.
     * @throws NoSuchFieldException Neither field (obf or deobf) exist, according to the client.
     */
    public void reloadResourcePack() throws NoSuchFieldException, IllegalAccessException {
        Field resourceManagerField;
        try {
            resourceManagerField = Minecraft.class.getDeclaredField("ay");
        } catch(NoSuchFieldException e) {
            resourceManagerField = Minecraft.class.getDeclaredField("mcResourceManager");
        }
        resourceManagerField.setAccessible(true);
        SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) resourceManagerField.get(Minecraft.getMinecraft());

        resourceManager.reloadResourcePack(resourcePack);
    }

    /**
     * Reload the provided resourceLocation with the provided file
     * @param file The file of the newly changed resource
     * @param resourceLocation The resourceLocation to change/set
     */
    public void reloadResource(File file, ResourceLocation resourceLocation) {
        if (file != null && file.exists()) {

            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            texturemanager.deleteTexture(resourceLocation);
            ITextureObject object = new ThreadDownloadImageData(file, null, resourceLocation, null);
            texturemanager.loadTexture(resourceLocation, object);
        }
    }
}
