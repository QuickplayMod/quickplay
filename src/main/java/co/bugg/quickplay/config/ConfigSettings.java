package co.bugg.quickplay.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
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

    @GuiOption(
            name = "quickplay.settings.primaryColor.name",
            helpText = "quickplay.settings.primaryColor.help",
            category = "quickplay.settings.category.colors"
    )
    public QuickplayColor primaryColor = new QuickplayColor(1.0f, 1.0f, 1.0f);
    @GuiOption(
            name = "quickplay.settings.secondaryColor.name",
            helpText = "quickplay.settings.secondaryColor.help",
            category = "quickplay.settings.category.colors"
    )
    public QuickplayColor secondaryColor = new QuickplayColor(0.7f, 0.7f, 0.7f);

    @GuiOption(
            name = "quickplay.settings.blurGuiBackgrounds.name",
            helpText = "quickplay.settings.blurGuiBackgrounds.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean blurGuiBackgrounds = true;

    @GuiOption(
            name = "quickplay.settings.fadeInGuis.name",
            helpText = "quickplay.settings.fadeInGuis.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean fadeInGuis = true;

    @GuiOption(
            name = "quickplay.settings.gameLogoScale.name",
            helpText = "quickplay.settings.gameLogoScale.help",
            category = "quickplay.settings.category.gui",
            minValue = 0.05f,
            maxValue = 2.5f
    )
    public double gameLogoScale = 1.0;

    @GuiOption(
            name = "quickplay.settings.reverseScrollingDirection.name",
            helpText = "quickplay.settings.reverseScrollingDirection.help",
            category = "quickplay.settings.category.gui"
    )
    public boolean reverseScrollingDirection = false;

    @GuiOption(
            name = "quickplay.settings.instanceDisplay.name",
            helpText = "quickplay.settings.instanceDisplay.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public boolean displayInstance = true;

    @GuiOption(
            name = "quickplay.settings.moveInstanceDisplayButton.name",
            helpText = "quickplay.settings.moveInstanceDisplayButton.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public transient final Runnable moveInstanceDisplayButton = () -> Quickplay.INSTANCE.instanceDisplay.edit();

    @GuiOption(
            name = "quickplay.settings.instanceOpacity.name",
            helpText = "quickplay.settings.instanceOpacity.help",
            category = "quickplay.settings.category.instanceDisplay",
            minValue = 0.0f,
            maxValue = 1.0f
    )
    public double instanceOpacity = 1.0;

    @GuiOption(
            name = "quickplay.settings.displayInstanceWithChatOpen.name",
            helpText = "quickplay.settings.displayInstanceWithChatOpen.help",
            category = "quickplay.settings.category.instanceDisplay"
    )
    public boolean displayInstanceWithChatOpen = true;

    // Hashmap of custom game priorities for the main game selection GUI
    public HashMap<String, Integer> gamePriorities = new HashMap<>();

    @GuiOption(
            name = "quickplay.settings.sendUsageStatsButton.name",
            helpText = "quickplay.settings.sendUsageStatsButton.help"
    )
    public transient final Runnable sendUsageStatsButton = () -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiUsageStats());

    @GuiOption(
            name = "quickplay.settings.editKeybinds.name",
            helpText = "quickplay.settings.editKeybinds.help"
    )
    public transient final Runnable editKeybinds = () -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiKeybinds());

    @GuiOption(
            name="quickplay.settings.lobbyOneSwap.name",
            helpText = "quickplay.settings.lobbyOneSwap.help"
    )
    public boolean lobbyOneSwap = false;

    @GuiOption(
            name = "quickplay.settings.updateNotifications.name",
            helpText = "quickplay.settings.updateNotifications.help"
    )
    public boolean updateNotifications = true;

    @GuiOption(
            name = "quickplay.settings.redesignedLobbyCommand.name",
            helpText = "quickplay.settings.redesignedLobbyCommand.help"
    )
    public boolean redesignedLobbyCommand = true;

    @GuiOption(
            name = "quickplay.settings.partyModeDelay.name",
            helpText = "quickplay.settings.partyModeDelay.help",
            category = "quickplay.settings.category.partyMode",
            minValue = 0,
            maxValue = 10,
            decimalFormat = "0.0"
    )
    public double partyModeDelay = 5.0;

    @GuiOption(
            name="quickplay.settings.partyModeGui.name",
            helpText = "quickplay.settings.partyModeGui.help",
            category = "quickplay.settings.category.partyMode"
    )
    public boolean partyModeGui = true;

    /**
     * Whether the client's own glyph should be visible or not
     * Should be set via QuickplayPremium jar.
     */
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
