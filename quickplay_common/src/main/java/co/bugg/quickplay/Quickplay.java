package co.bugg.quickplay;

import co.bugg.quickplay.actions.serverbound.ExceptionThrownAction;
import co.bugg.quickplay.client.command.CommandHub;
import co.bugg.quickplay.client.command.CommandMain;
import co.bugg.quickplay.client.command.CommandQuickplay;
import co.bugg.quickplay.client.gui.InstanceDisplay;
import co.bugg.quickplay.client.gui.QuickplayGuiPartySpinner;
import co.bugg.quickplay.client.render.GlyphRenderer;
import co.bugg.quickplay.client.render.PlayerGlyph;
import co.bugg.quickplay.config.*;
import co.bugg.quickplay.elements.Button;
import co.bugg.quickplay.elements.ElementController;
import co.bugg.quickplay.http.HttpRequestFactory;
import co.bugg.quickplay.http.SocketClient;
import co.bugg.quickplay.util.*;
import co.bugg.quickplay.util.analytics.AnalyticsRequest;
import co.bugg.quickplay.util.analytics.GoogleAnalytics;
import co.bugg.quickplay.util.analytics.GoogleAnalyticsFactory;
import co.bugg.quickplay.util.buffer.ChatBuffer;
import co.bugg.quickplay.util.buffer.MessageBuffer;
import co.bugg.quickplay.wrappers.MinecraftWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import co.bugg.quickplay.wrappers.chat.IChatComponentWrapper;
import com.google.gson.JsonSyntaxException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Quickplay {

    /**
     * Instance of Quickplay
     */
    public static final Quickplay INSTANCE = new Quickplay();
    /**
     * Quickplay logger
     */
    public static final Logger LOGGER = Logger.getLogger("Quickplay");
    /**
     * Instance of Forge mod for Quickplay
     */
    public QuickplayMod mod;
    /**
     * Wrapper for Minecraft's main class.
     */
    public MinecraftWrapper minecraft = new MinecraftWrapper();
    /**
     * Whether the mod is currently enabled
     */
    public boolean isEnabled = false;
    /**
     * Whether the client is currently in debug mode. Extra data is printed to the console in debug mode.
     * @see co.bugg.quickplay.client.command.SubCommandDebug
     */
    public boolean isInDebugMode = false;
    /**
     * The reason the mod has been disabled, if it is disabled
     */
    public String disabledReason = null;
    /**
     * The recognized server that the user is currently on, according to the Quickplay backend.
     * This is used for determining whether certain buttons/actions/screens should be visible/executable at any
     * given moment or not, under the "availableOn" array. If this is within the "availableOn" array of an item, then
     * that item should be "usable". If the "availableOn" array is empty, then this value isn't checked for that item.
     */
    public String currentServer = null;
    /**
     * Hypixel staff rank in the form of e.g. HELPER, ADMIN, YOUTUBER, etc. Used for determining whether some
     * commands which require a rank should be displayed or not. This is modified when
     * {@link co.bugg.quickplay.actions.clientbound.AuthCompleteAction} is received.
     */
    public String hypixelRank = null;
    /**
     * Hypixel package rank in the form of e.g. NONE, VIP_PLUS, SUPERSTAR, etc. Used for determining whether some
     * commands which require a rank should be displayed or not. This is modified when
     * {@link co.bugg.quickplay.actions.clientbound.AuthCompleteAction} is received.
     */
    public String hypixelPackageRank = null;
    /**
     * Flag signifying whether the user is a Hypixel build team member or not. This is modified when
     * {@link co.bugg.quickplay.actions.clientbound.AuthCompleteAction} is received.
     */
    public boolean isHypixelBuildTeamMember = false;
    /**
     * Flag signifying whether the user is a Hypixel build team admin or not. This is modified when
     * {@link co.bugg.quickplay.actions.clientbound.AuthCompleteAction} is received.
     */
    public boolean isHypixelBuildTeamAdmin = false;
    /**
     * Thread pool for blocking code
     */
    public final ExecutorService threadPool = Executors.newCachedThreadPool();
    /**
     * A list of all registered event handlers
     */
    public final List<Object> eventHandlers = new ArrayList<>();
    /**
     * Factory for creating HTTP requests
     * TODO remove
     */
    @Deprecated
    public HttpRequestFactory requestFactory;
    /**
     * Socket client connected to the Quickplay backend
     */
    public SocketClient socket;
    /**
     * Factory for creating, loading, etc. of mod assets
     */
    public AssetFactory assetFactory;
    /**
     * Controller of Quickplay elements
     */
    public ElementController elementController = new ElementController();
    /**
     * InstanceWatcher that constantly watches for what Hypixel server instance the client is on
     */
    public HypixelInstanceWatcher hypixelInstanceWatcher;
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
     * TODO remove
     */
    public ConfigUsageStats usageStats;
    /**
     * Whether the user should be asked if they want to share stats next time they join a server
     * TODO remove
     */
    public boolean promptUserForUsageStats = false;
    /**
     * Help menu for Quickplay Premium
     * retrieved from the <code>enable</code> endpoint on mod enable from the content field <code>premiumInfo</code>
     */
    public IChatComponentWrapper premiumAbout = null;
    /**
     * List of all player glyphs, which contains the URL to the glyph as well as the owner's UUID
     */
    public List<PlayerGlyph> glyphs = new ArrayList<>();
    /**
     * Google Analytics API tracker
     */
    public GoogleAnalytics ga;
    /**
     * Whether this client has administrative privileges or not.
     * Admins are able to make modifications to the games list, see
     * restricted information, and interact with buttons which are
     * marked as admin-only.
     */
    public boolean isAdminClient = false;
    /**
     * Whether this client is premium or not. Premium clients
     * are able to use some features which are otherwise locked.
     */
    public boolean isPremiumClient = false;
    /**
     * When Quickplay Premium expires for this user
     */
    public Date premiumExpirationDate = new Date(0);
    /**
     * URL to this Premium user's purchase page.
     */
    public String purchasePageURL = null;
    /**
     * Session key used for Premium-related resource requests
     */
    public String sessionKey;

    /**
     * Hook into the Forge pre-initialization call.
     */
    public void preInit() {
        final String formatStr = "[%02d:%02d:%02d] [Quickplay/%s]: %s\n";
        final Calendar calendar = Calendar.getInstance();
        Quickplay.LOGGER.setUseParentHandlers(false);

        final StreamHandler infoHandler = new StreamHandler(System.out, new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                calendar.setTimeInMillis(record.getMillis());
                return String.format(formatStr, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND), record.getLevel().getLocalizedName(), record.getMessage());
            }
        }) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                this.flush();
            }
        };
        infoHandler.setLevel(Level.ALL);
        infoHandler.setFilter(record -> record.getLevel().intValue() < Level.WARNING.intValue());;

        final StreamHandler errHandler = new StreamHandler(System.err, new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                calendar.setTimeInMillis(record.getMillis());
                return String.format(formatStr, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND), record.getLevel().getLocalizedName(), record.getMessage());
            }
        }) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                this.flush();
            }
        };
        errHandler.setLevel(Level.WARNING);

        Quickplay.LOGGER.addHandler(infoHandler);
        Quickplay.LOGGER.addHandler(errHandler);

        if(Objects.equals(System.getenv("QUICKPLAY_DEBUG"), "1")) {
            this.setDebugMode(true);
            Quickplay.LOGGER.fine("Quickplay debug mode enabled via environment variable.");
        }
    }

    /**
     * Hook into the Forge initialization call.
     */
    public void init() {
        // The message buffer should remain online even
        // if the mod is disabled - this allows for
        // communicating important information about why
        // the mod is currently disabled, or how to fix.
        this.minecraft.messageBuffer = (MessageBuffer) new MessageBuffer(100).start();
        try {
            this.enable();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            this.sendExceptionRequest(e);
            this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.failedToEnable"),
                    true, true));
        }
    }

    /**
     * Register an event handler with both Forge and Quickplay.
     * @param handler Handler to register.
     */
    public void registerEventHandler(Object handler) {
        if(!this.eventHandlers.contains(handler)) {
            this.eventHandlers.add(handler);
        }
        this.mod.registerEventHandler(handler);
    }

    /**
     * Unregister an event handler which is registered both with Forge and Quickplay.
     * @param handler Handler to unregister.
     */
    public void unregisterEventHandler(Object handler) {
        this.eventHandlers.remove(handler);
        this.mod.unregisterEventHandler(handler);
    }


    /**
     * Loads settings config from minecraft installation folder.
     * If not present, creates new config file
     */
    private void loadSettings() {
        try {
            this.settings = (ConfigSettings) AConfiguration.load("settings.json", ConfigSettings.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            this.assetFactory.createDirectories();
            this.settings = new ConfigSettings();
            try {
                // Write the default config that we just made to save it
                this.settings.save();
            } catch (IOException e1) {
                // File couldn't be saved
                e1.printStackTrace();
                this.sendExceptionRequest(e1); // TODO replace
                this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.config.saveError").setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
            }
        }
    }

    /**
     * Loads usage stats config from minecraft installation folder.
     * If not present, sets flag requiring the user to opt-in or opt-out.
     */
    private void loadUsageStats() {
        try {
            this.usageStats = (ConfigUsageStats) AConfiguration.load("privacy.json", ConfigUsageStats.class);
        } catch (IOException | JsonSyntaxException e) {
            this.promptUserForUsageStats = true;
        }
    }


    /**
     * Loads keybinds config from minecraft installation folder.
     * If using an outdated format (i.e. format from prior to 2.1.0), sends MigrateKeybindsAction to the server. Also
     * creates a backup of the current keybinds, and notifies the user of the migration.
     * If not present, creates new config.
     */
    private void loadKeybinds() {
        // If migration to the latest keybinds system is necessary, we only notify the user that migration will happen here
        // and create a backup. The actual migration process is postponed until the socket successfully connects.
        if(ConfigKeybinds.checkForConversionNeeded("keybinds.json")) {
            // Create backup
            long now = new Date().getTime();
            try {
                AConfiguration.createBackup("keybinds.json", "keybinds-backup-" + now + ".json");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Tell user about migration
            this.minecraft.sendLocalMessage(new Message(
                    new QuickplayChatComponentTranslation("quickplay.keybinds.migrating")
                            .setStyle(new ChatStyleWrapper().apply(Formatting.GRAY))));
            // Keybinds are set to default temporarily. The user's instructed not to modify their keybinds until
            // migration is complete, as that would trigger a save.
            this.keybinds = new ConfigKeybinds(true);

            // If migration isn't complete after 30 seconds, it's assumed the migration failed.
            this.threadPool.submit(() -> {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(ConfigKeybinds.checkForConversionNeeded("keybinds.json")) {
                    this.minecraft.sendLocalMessage(new Message(
                            new QuickplayChatComponentTranslation("quickplay.keybinds.migratingFailedServerOffline")
                                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
                            , true));
                }
            });
            return;
        }

        // At this point, migration must not be necessary, so keybinds are loaded as normal.
        try {
            this.keybinds = (ConfigKeybinds) AConfiguration.load("keybinds.json", ConfigKeybinds.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            this.assetFactory.createDirectories();
            this.keybinds = new ConfigKeybinds(true);
            try {
                // Write the default config that we just made to save it
                this.keybinds.save();
            } catch (IOException e1) {
                // File couldn't be saved
                e1.printStackTrace();
                this.sendExceptionRequest(e1); // TODO replace
                this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.config.saveError").setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
            }
        }
    }

    /**
     * Enable the mod
     */
    public void enable() throws URISyntaxException {
        if(!this.isEnabled) {
            this.isEnabled = true;
            this.requestFactory = new HttpRequestFactory(); // TODO remove

            this.assetFactory = new AssetFactory();

            this.assetFactory.createDirectories();
            this.assetFactory.dumpOldCache();
            this.mod.resourcePack = this.assetFactory.registerResourcePack();

            this.loadSettings();
            this.loadUsageStats();
            this.loadKeybinds();
            this.socket = new SocketClient(new URI(Reference.BACKEND_SOCKET_URI));
            this.socket.connect();

            // Create new Google Analytics instance if possible
            if(this.usageStats != null && this.usageStats.statsToken != null) {
                this.createGoogleAnalytics();
            }

            // Send analytical data to Google
            if(this.usageStats != null && this.usageStats.statsToken != null && this.usageStats.sendUsageStats && ga != null) {
                this.threadPool.submit(() -> {
                    try {
                        this.ga.createEvent("Systematic Events", "Mod Enable")
                                .setSessionControl(AnalyticsRequest.SessionControl.START)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            // TODO listen for language changes and fire LanguageChangedActions

            this.registerEventHandler(new GlyphRenderer());
            this.registerEventHandler(new QuickplayEventHandler());

            this.minecraft.chatBuffer = (ChatBuffer) new ChatBuffer(200, 8, 5000, 1000).start();
            this.hypixelInstanceWatcher = new HypixelInstanceWatcher().start();
            this.instanceDisplay = new InstanceDisplay(this.hypixelInstanceWatcher);

            this.mod.commands.add(new CommandQuickplay());

            if(this.settings.redesignedLobbyCommand) {
                // Register lobby commands
                this.mod.commands.add(new CommandHub("l"));
                this.mod.commands.add(new CommandHub("lobby"));
                this.mod.commands.add(new CommandHub("hub"));
                this.mod.commands.add(new CommandHub("spawn"));
                this.mod.commands.add(new CommandHub("leave"));
                this.mod.commands.add(new CommandMain("main"));
            }
            // Copy of the lobby command that doesn't override server commands
            // Used for "Go to Lobby" buttons
            this.mod.commands.add(new CommandHub("quickplaylobby", "lobby"));
            this.mod.registerCommands();
        }
    }

    /**
     * Disable the mod
     */
    public void disable(String reason) {
        if(this.isEnabled) {
            this.isEnabled = false;
            while(this.eventHandlers.size() > 0) {
                this.unregisterEventHandler(this.eventHandlers.get(0));
            }
            this.disabledReason = reason;

            if(this.socket != null) {
                this.socket.close();
                this.socket = null;
            }

            if(this.minecraft.chatBuffer != null) {
                this.minecraft.chatBuffer.stop();
                this.minecraft.chatBuffer = null;
            }

            if(this.hypixelInstanceWatcher != null) {
                this.hypixelInstanceWatcher.stop();
                this.hypixelInstanceWatcher = null;
            }

            this.isPremiumClient = false;
            this.isAdminClient = false;
            this.sessionKey = null;
        }
    }

    /**
     * Check if the mod is enabled, and
     * send a disabled message if not.
     * @return Whether the mod is enabled
     */
    public boolean checkEnabledStatus() {
        if(!this.isEnabled) {
            IChatComponentWrapper msg = new QuickplayChatComponentTranslation("quickplay.disabled", this.disabledReason);
            msg.setStyle(new ChatStyleWrapper().apply(Formatting.RED));
            this.minecraft.sendLocalMessage(new Message(msg, true));
        }

        return this.isEnabled;
    }

    /**
     * Start a party mode session by randomizing selected games & then executing the command
     */
    public void launchPartyMode() {
        // If there are no buttons known to Quickplay, then just tell the user he has no games selected.
        if(this.elementController == null || this.elementController.buttonMap == null || this.elementController.buttonMap.size() <= 0) {
            this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.party.noGames")
                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
        }

        Set<String> enabledButtons;
        // If the user has no games enabled in their config, then we assume they have all games enabled. This means
        // that when more games are added in the future, they'll automatically be enabled.
        if(this.settings.enabledButtonsForPartyMode == null || this.settings.enabledButtonsForPartyMode.size() <= 0) {
            enabledButtons = new HashSet<>(this.elementController.buttonMap.keySet());
        } else {
            enabledButtons = new HashSet<>(this.settings.enabledButtonsForPartyMode);
        }

        if(enabledButtons.size() > 0) {
            if(this.settings.partyModeGui) {
                this.minecraft.openGui(new QuickplayGuiPartySpinner());
            } else {
                // No GUI, handle randomization in chat
                // If there is a delay greater than 0 seconds, we send a message so the user knows something happened.
                if(this.settings.partyModeDelay > 0) {
                    this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.party.commencing")
                            .setStyle(new ChatStyleWrapper().apply(Formatting.LIGHT_PURPLE))));
                }

                // Pick a random button from our list of enabled buttons that exists and is visible to party mode.
                Button button = null;
                while(button == null || !button.visibleInPartyMode || !button.passesPermissionChecks()) {
                    String buttonKey;
                    if(enabledButtons.size() == 1) {
                        buttonKey = enabledButtons.stream().findFirst().orElse(null);
                    } else {
                        final Random random = new Random();
                        buttonKey = enabledButtons.stream().skip(random.nextInt(enabledButtons.size()))
                                .findFirst().orElse(null);
                    }
                    button = this.elementController.getButton(buttonKey);

                    // If the randomly selected button can't be used in party mode, remove it from our list and try again.
                    // If there are no more buttons to pick, then stop and send an error to the user.
                    if(button == null || !button.visibleInPartyMode || !button.passesPermissionChecks()) {
                        enabledButtons.remove(buttonKey);
                        if(enabledButtons.size() == 0) {
                            this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.party.noGames")
                                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
                            return;
                        }
                    }
                }

                try {
                    Thread.sleep((long) (this.settings.partyModeDelay * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String translatedGame = this.elementController.translate(button.translationKey);
                if(button.partyModeScopeTranslationKey != null && button.partyModeScopeTranslationKey.length() > 0) {
                    translatedGame = this.elementController.translate(button.partyModeScopeTranslationKey) + " - " +
                            translatedGame;
                }
                this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.party.sendingYou",
                        translatedGame).setStyle(new ChatStyleWrapper().apply(Formatting.GREEN))));
                button.run();
            }
        } else {
            this.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.party.noGames")
                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
        }
    }

    /**
     * Send an exception request to Quickplay backend for error reporting
     * @param e Exception that occurred
     */
    public void sendExceptionRequest(Exception e) {
        if(this.usageStats != null && this.usageStats.sendUsageStats) {
            this.threadPool.submit(() -> {
                try {
                    this.socket.sendAction(new ExceptionThrownAction(e));
                } catch (ServerUnavailableException serverUnavailableException) {
                    serverUnavailableException.printStackTrace();
                }
                if(this.ga != null) {
                    try {
                        this.ga.createException().setExceptionDescription(e.getMessage()).send();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        }
    }

    public void setDebugMode(boolean state) {
        this.isInDebugMode = state;
        if(state) {
            Quickplay.LOGGER.setLevel(Level.ALL);
        } else {
            Quickplay.LOGGER.setLevel(Level.INFO);
        }
    }

    /**
     * Create the Google Analytics instance with customized settings for this Quickplay instance
     */
    public void createGoogleAnalytics() {
        this.ga = GoogleAnalyticsFactory.create(Reference.ANALYTICS_TRACKING_ID, usageStats.statsToken.toString(),
                Reference.MOD_NAME, Reference.VERSION);
        final AnalyticsRequest defaultRequest = ga.getDefaultRequest();

        defaultRequest.setLanguage(String.valueOf(this.minecraft.getLanguage()).toLowerCase());

        final GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        defaultRequest.setScreenResolution(screen.getDisplayMode().getWidth() + "x" + screen.getDisplayMode().getHeight());

        // Determine User-Agent/OS
        // Example: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36
        final String systemDetails = System.getProperty("os.name") + " " + System.getProperty("os.version") + "; " + System.getProperty("os.arch");
        defaultRequest.setUserAgent("Mozilla/5.0 (" + systemDetails + ") " + Reference.MOD_NAME + " " + Reference.VERSION);
    }

    /**
     * Get whether the user is currently on the Hypixel network. Also returns true if the client is connected to a
     * Minecraft server and the currentServer is null and not connected to the backend and/or not authed.
     * This is so Hypixel is the default network if the Quickplay backend is offline.
     * @return True if this.currentServer is null or contains "hypixel" (case insensitive)
     */
    public boolean isOnHypixel() {
        if(this.currentServer == null) {
            return ServerChecker.getCurrentIP() != null && (this.socket == null || this.socket.isClosed() || this.sessionKey == null);
        }
        return this.currentServer.toLowerCase().contains("hypixel");
    }
}
