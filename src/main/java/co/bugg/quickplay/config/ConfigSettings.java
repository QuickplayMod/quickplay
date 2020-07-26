package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.MoveableHudElement;
import co.bugg.quickplay.client.gui.config.QuickplayGuiKeybinds;
import co.bugg.quickplay.client.gui.config.QuickplayGuiUsageStats;
import co.bugg.quickplay.games.PartyMode;
import net.minecraft.client.Minecraft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigSettings extends AConfiguration implements Serializable {

    // TODO make runnables into methods
    public ConfigSettings() {
        super("settings.json");
    }

    /**
     * Ratio from the left of the screen that the instance display should be drawn at
     */
    public double instanceDisplayX = 0.5;
    /**
     * Ratio from the top of the screen that the instance display should be drawn at
     */
    public double instanceDisplayY = 0.05;

    /**
     * Quickplay's primary color
     */
    @GuiOption(
            name = "quickplay.settings.primaryColor.name",
            helpText = "quickplay.settings.primaryColor.help",
            category = "quickplay.settings.category.colors"
    )
    public QuickplayColor primaryColor = new QuickplayColor(1.0f, 1.0f, 1.0f);

    /**
     * Quickplay's secondary color
     */
    @GuiOption(
            name = "quickplay.settings.secondaryColor.name",
            helpText = "quickplay.settings.secondaryColor.help",
            category = "quickplay.settings.category.colors"
    )
    public QuickplayColor secondaryColor = new QuickplayColor(0.7f, 0.7f, 0.7f);

    /**
     * Whether Quickplay GUIs should be blurred
     */
    @GuiOption(
            name = "quickplay.settings.blurGuiBackgrounds.name",
            helpText = "quickplay.settings.blurGuiBackgrounds.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean blurGuiBackgrounds = true;

    /**
     * Whether Quickplay GUIs should fade in
     */
    @GuiOption(
            name = "quickplay.settings.fadeInGuis.name",
            helpText = "quickplay.settings.fadeInGuis.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean fadeInGuis = true;

    /**
     * Whether Quickplay GUI backgrounds should be 100% transparent
     */
    @GuiOption(
            name="quickplay.settings.transparentBackgrounds.name",
            helpText = "quickplay.settings.transparentBackgrounds.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean transparentBackgrounds = false;

    /**
     * The scale of items in the Quickplay main menu
     */
    @GuiOption(
            name = "quickplay.settings.gameLogoScale.name",
            helpText = "quickplay.settings.gameLogoScale.help",
            category = "quickplay.settings.category.gui",
            minValue = 0.05f,
            maxValue = 2.5f
    )
    public double gameLogoScale = 1.0;

    /**
     * Whether scrolling in GUIs should be reversed from the default
     */
    @GuiOption(
            name = "quickplay.settings.reverseScrollingDirection.name",
            helpText = "quickplay.settings.reverseScrollingDirection.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean reverseScrollingDirection = false;

    /**
     * Whether any key should close the Quickplay main menu & game GUIs or only escape and inventory key
     * This does not affect any other GUI on Quickplay or otherwise
     */
    @GuiOption(
            name="quickplay.settings.anyKeyClosesGui.name",
            helpText="quickplay.settings.anyKeyClosesGui.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean anyKeyClosesGui = false;

    /**
     * Whether the Quickplay Main Menu should be compacted to hide strings & only display game
     * names when you hover over the game's icon
     */
    @GuiOption(
            name="quickplay.settings.compactMainMenu.name",
            helpText = "quickplay.settings.compactMainMenu.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean compactMainMenu = false;
    /**
     * Whether the Quickplay Main Menu colors should be swapped so secondary
     * colors are non-hover and primary colors are hover.
     */
    @GuiOption(
            name="quickplay.settings.swapMainGuiColors.name",
            helpText = "quickplay.settings.swapMainGuiColors.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean swapMainGuiColors = false;

    /**
     * Whether Quickplay's instance displayer should be displayed
     */
    @GuiOption(
            name = "quickplay.settings.instanceDisplay.name",
            helpText = "quickplay.settings.instanceDisplay.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public boolean displayInstance = false;

    /**
     * Runnable to move the instance display to a different location
     */
    @GuiOption(
            name = "quickplay.settings.moveInstanceDisplayButton.name",
            helpText = "quickplay.settings.moveInstanceDisplayButton.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public transient final Runnable moveInstanceDisplayButton = () -> Quickplay.INSTANCE.instanceDisplay.edit();

    /**
     * Opacity of the instance display
     */
    @GuiOption(
            name = "quickplay.settings.instanceOpacity.name",
            helpText = "quickplay.settings.instanceOpacity.help",
            category = "quickplay.settings.category.instanceDisplay",
            minValue = 0.0f,
            maxValue = 1.0f
    )
    public double instanceOpacity = 1.0;

    /**
     * Whether the instance display should be visible even when chat is open
     */
    @GuiOption(
            name = "quickplay.settings.displayInstanceWithChatOpen.name",
            helpText = "quickplay.settings.displayInstanceWithChatOpen.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public boolean displayInstanceWithChatOpen = true;

    /**
     * The scale of the Quickplay Instance Display
     */
    @GuiOption(
            name = "quickplay.settings.instanceDisplayScale.name",
            helpText = "quickplay.settings.instanceDisplayScale.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public MoveableHudElement.Size instanceDisplayScale = MoveableHudElement.Size.MEDIUM;

    /**
     * Hashmap of custom game priorities for the main game selection GUI
     *
     * Key is the game's unlocalized name, value is the priority (higher number = higher priority)
     * Cannot be customized outside of a text editor at the moment
     */
    public HashMap<String, Integer> gamePriorities = new HashMap<>();

    /**
     * Runnable to change the privacy settings
     */
    @GuiOption(
            name = "quickplay.settings.sendUsageStatsButton.name",
            helpText = "quickplay.settings.sendUsageStatsButton.help"
    )
    public transient final Runnable sendUsageStatsButton = () -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiUsageStats());

    /**
     * Runnable to edit the keybinds
     */
    @GuiOption(
            name = "quickplay.settings.editKeybinds.name",
            helpText = "quickplay.settings.editKeybinds.help"
    )
    public transient final Runnable editKeybinds = () -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());

    /**
     * Whether the client should swap to lobby one when it joins a new lobby
     */
    @GuiOption(
            name="quickplay.settings.lobbyOneSwap.name",
            helpText = "quickplay.settings.lobbyOneSwap.help"
    )
    public boolean lobbyOneSwap = false;

    /**
     * Whether the client should receive notifications for updates
     */
    @GuiOption(
            name = "quickplay.settings.updateNotifications.name",
            helpText = "quickplay.settings.updateNotifications.help"
    )
    public boolean updateNotifications = true;

    /**
     * Whether the client should use Quickplay's redesigned /hub command
     */
    @GuiOption(
            name = "quickplay.settings.redesignedLobbyCommand.name",
            helpText = "quickplay.settings.redesignedLobbyCommand.help"
    )
    public boolean redesignedLobbyCommand = true;

    /**
     * The delay in the number of seconds for party mode to "calculate"
     * Cosmetic, used for suspense & for the spinner
     */
    @GuiOption(
            name = "quickplay.settings.partyModeDelay.name",
            helpText = "quickplay.settings.partyModeDelay.help",
            category = "quickplay.settings.category.partyMode",
            minValue = 0,
            maxValue = 10,
            decimalFormat = "0.0"
    )
    public double partyModeDelay = 5.0;

    /**
     * Whether the client should use the party mode spinner GUI or not
     */
    @GuiOption(
            name = "quickplay.settings.partyModeGui.name",
            helpText = "quickplay.settings.partyModeGui.help",
            category = "quickplay.settings.category.partyMode"
    )
    public boolean partyModeGui = true;

    /**
     * Whether the user's daily reward should be displayed in-game rather than in-browser
     */
    @GuiOption(
            name = "quickplay.settings.ingameDailyReward.name",
            helpText = "quickplay.settings.ingameDailyReward.help",
            category = "quickplay.settings.category.premium"
    )
    public boolean ingameDailyReward = true;

    /**
     * Whether the client's own glyph should be visible or not
     * Should be set via QuickplayPremium jar.
     */
    @GuiOption(
            name = "quickplay.settings.displayOwnGlyph.name",
            helpText = "quickplay.settings.displayOwnGlyph.help",
            category = "quickplay.settings.category.premium"
    )
    public boolean displayOwnGlyph = true;

    /**
     * Allows users to change how much padding is on the
     * top & bottom of {@link co.bugg.quickplay.client.gui.game.QuickplayGuiMainMenu}
     *
     * Cannot be changed in the GUI as that might be confusing but
     * is there for the users who need it.
     *
     * Divided by two, half for top and half for bottom
     *
     * @see <a href="https://github.com/bugfroggy/Quickplay2.0/issues/3">Github issue</a>
     */
    public int mainMenuYPadding = 60;

    /**
     * A list of all modes currently set to "TRUE" in party mode.
     *
     * When the client receives a response from the gamelist endpoint,
     * it will verify all modes in this list and make sure they're all valid
     */
    public List<PartyMode> partyModes = new ArrayList<>();
}
