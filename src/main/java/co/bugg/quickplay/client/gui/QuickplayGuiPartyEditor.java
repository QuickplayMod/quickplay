package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.Mode;
import co.bugg.quickplay.games.PartyMode;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for editing the modes in party mode
 */
public class QuickplayGuiPartyEditor extends QuickplayGui {
    /**
     * A list of every single mode the client is aware of
     */
    List<PartyMode> modes = new ArrayList<>();
    /**
     * A list of all the modes toggled on
     * CURRENTLY A REFERENCE to the Quickplay settings, not a value
     */
    List<PartyMode> toggledModes = new ArrayList<>();

    /**
     * The width of buttons
     */
    public final int buttonWidth = 300;
    /**
     * The height of buttons
     */
    public final int buttonHeight = 20;
    /**
     * Vertical spacing between buttons
     */
    public final int buttonYMargins = 5;
    /**
     * The Y value at which point values should start being drawn
     */
    public int topOfButtons;
    /**
     * Across how many pixels buttons fade while scrolling
     */
    public final int scrollFadeDistance = 20;
    /**
     * Width of the three buttons along the top
     */
    public int topButtonWidth = 100;
    /**
     * Margins between the buttons along the top
     */
    public int topButtonMargins = 5;

    @Override
    public void initGui() {
        super.initGui();

        topOfButtons = (int) (height * 0.2);

        // Copy over reference to the toggled mode
        toggledModes = Quickplay.INSTANCE.settings.partyModes;

        // Create a list of all applicable modes
        for(Game game : Quickplay.INSTANCE.gameList) {
            if(game.unlocalizedName.equals("partyMode"))
                continue;
            for(Mode mode : game.modes) {
                modes.add(new PartyMode(game.name + " - " + mode.name, mode.command, game.unlocalizedName.replace("/", "") + "/" + mode.command.replace("/", "")));
            }
        }

        int buttonId = 0;
        // Add all the buttons for each mode
        for(PartyMode mode : modes) {
            // Display string for whether this mode is currently enabled or not
            final String trueOrFalse = checkIfModeToggled(mode) != null ? EnumChatFormatting.GREEN + I18n.format("quickplay.config.gui.true") : EnumChatFormatting.RED + I18n.format("quickplay.config.gui.false");
            componentList.add(new QuickplayGuiButton(mode, buttonId, width / 2 - buttonWidth / 2, topOfButtons + (buttonHeight + buttonYMargins) * buttonId, buttonWidth, buttonHeight, mode.name + ": " + trueOrFalse, true));
            buttonId++;
        }

        // Add launch, "All On" and "All Off" buttons
        componentList.add(new QuickplayGuiButton(null, buttonId++, width / 2  - topButtonWidth / 2, 10, topButtonWidth, buttonHeight, I18n.format("quickplay.gui.party.launch"), false)); // Launch
        componentList.add(new QuickplayGuiButton(null, buttonId++, width / 2 - topButtonWidth / 2 - topButtonWidth - topButtonMargins, 10, topButtonWidth, 20, I18n.format("quickplay.gui.party.allon"), false)); // All on
        componentList.add(new QuickplayGuiButton(null, buttonId++, width / 2 + topButtonWidth / 2 + topButtonMargins, 10, topButtonWidth, 20, I18n.format("quickplay.gui.party.alloff"), false)); // All off

        setScrollingValues();
    }

    /**
     * Checks if the given mode is currently toggled on or not according to {@link #toggledModes}
     * @param mode Mode to check
     * @return The reference to the mode in {@link #toggledModes}, or null if not in the list.
     */
    public PartyMode checkIfModeToggled(PartyMode mode) {
        return toggledModes.stream().filter(settingMode -> settingMode.namespace.equals(mode.namespace)).findFirst().orElse(null);
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        scrollFrameTop = topOfButtons;

        // Increase scroll speed & amount
        scrollMultiplier = 3;
        scrollDelay = 2;
        scrollbarYMargins = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        drawScrollbar(width / 2 + buttonWidth / 2 + 3);

        //Override super.drawScreen(mouseX, mouseY, partialTicks);
        for (QuickplayGuiComponent component : componentList) {
            double scrollOpacity = component.scrollable ? ((component.y - scrollPixel) > topOfButtons ? 1 : (component.y - scrollPixel) + scrollFadeDistance < topOfButtons ? 0 : (scrollFadeDistance - ((double) topOfButtons - (double) (component.y - scrollPixel))) / (double) scrollFadeDistance) : 1;
            component.draw(this, mouseX, mouseY, opacity * scrollOpacity);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof PartyMode) {
            if(component.y - scrollPixel > topOfButtons - scrollFadeDistance && component.scrollable) {
                final PartyMode mode = (PartyMode) component.origin;
                // Display name of this component, split at the colon, to separate the name from the current toggle status
                final String nameWithoutToggleStatus = component.displayString.split(":")[0];

                // Reference to this mode's twin in the toggled modes list (or null if doesn't exist)
                final PartyMode toggledModeReference = checkIfModeToggled(mode);
                // If the mode is toggled on remove it, otherwise add it
                if(toggledModeReference != null) {
                    toggledModes.remove(toggledModeReference);
                    component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.RED + I18n.format("quickplay.config.gui.false");
                } else {
                    toggledModes.add(mode);
                    component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.GREEN + I18n.format("quickplay.config.gui.true");
                }
            }
        } else if(component.displayString.equals(I18n.format("quickplay.gui.party.alloff"))) {
            // Disable all
            toggledModes.clear();
            initGui();
        } else if(component.displayString.equals(I18n.format("quickplay.gui.party.allon"))) {
            // Enable all
            toggledModes.clear();
            toggledModes.addAll(modes);
            initGui();
        } else if(component.displayString.equals(I18n.format("quickplay.gui.party.launch"))) {
            // Launch!
            Minecraft.getMinecraft().displayGuiScreen(null);
            Quickplay.INSTANCE.threadPool.submit(Quickplay.INSTANCE::launchPartyMode);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        try {
            Quickplay.INSTANCE.settings.save();
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.config.saveerror")));
        }
    }
}
