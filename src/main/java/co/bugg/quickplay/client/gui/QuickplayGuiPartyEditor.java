package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Button;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.*;

/**
 * GUI screen for editing the modes in party mode
 */
public class QuickplayGuiPartyEditor extends QuickplayGui {
    /**
     * A list of all the modes toggled on
     * CURRENTLY A REFERENCE to the Quickplay settings, not a value
     */
    Set<String> enabledButtons = new HashSet<>();
    Set<String> allButtons = new HashSet<>();

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
        this.topOfButtons = (int) (height * 0.2);

        // TODO add loading screen -- Lots of sorting within this function which may cause delays.

        if(Quickplay.INSTANCE.buttonMap == null || Quickplay.INSTANCE.buttonMap.size() <= 0) {
            // TODO handle error
            return;
        }

        this.allButtons = new HashSet<>(Quickplay.INSTANCE.buttonMap.keySet());
        // If no buttons are specifically "enabled", then the default assumption is that all buttons are enabled.
        if(Quickplay.INSTANCE.settings.enabledButtonsForPartyMode == null ||
                Quickplay.INSTANCE.settings.enabledButtonsForPartyMode.size() <= 0) {
            this.enabledButtons = new HashSet<>(this.allButtons);
        } else {
            // If some buttons are explicitly enabled, copy them into a new hashset. The original set is not
            // modified until the GUI closes, at which point it's replaced.
            this.enabledButtons = new HashSet<>(Quickplay.INSTANCE.settings.enabledButtonsForPartyMode);
        }

        this.addButtons();
        this.setScrollingValues();
    }

    /**
     * Add all the button components to this GUI. This is called on GUI initialization and whenever the list needs
     * to be refreshed, i.e. when all buttons are enabled/disabled.
     */
    public void addButtons() {
        int buttonId = 0;
        // Convert the set of button keys into an alphabetically sorted list of button display titles
        final List<String> allButtonsAsSortedList = new ArrayList<>(this.allButtons);
        allButtonsAsSortedList.sort((a,b) -> {
            Button buttonA = Quickplay.INSTANCE.buttonMap.get(a);
            Button buttonB = Quickplay.INSTANCE.buttonMap.get(b);

            if(buttonA == null) {
                return 1;
            } else if(buttonB == null) {
                return -1;
            }

            String buttonATitle = Quickplay.INSTANCE.translator.get(buttonA.translationKey);
            String buttonBTitle = Quickplay.INSTANCE.translator.get(buttonB.translationKey);
            return buttonATitle.compareTo(buttonBTitle);
        });

        for(String buttonKey : allButtonsAsSortedList) {
            Button button = Quickplay.INSTANCE.buttonMap.get(buttonKey);
            if(button == null || !button.visibleInPartyMode) {
                continue;
            }
            // Buttons are visible in this editor regardless of current Hypixel location, but not ranks or current server.
            if(!button.visible || !button.passesRankChecks() || !button.passesServerCheck()) {
                continue;
            }

            // Display-string for whether this mode is currently enabled or not
            final String trueOrFalse = this.enabledButtons.contains(buttonKey) ?
                    EnumChatFormatting.GREEN + Quickplay.INSTANCE.translator.get("quickplay.config.gui.true") :
                    EnumChatFormatting.RED + Quickplay.INSTANCE.translator.get("quickplay.config.gui.false");
            String buttonText = Quickplay.INSTANCE.translator.get(button.translationKey) + ": " + trueOrFalse;
            // If a button doesn't pass the Hypixel location checks (all other permission checks passed), then
            // the button still displays, but it's made clear to the user that the button won't work in the current location.
            if(!button.passesPermissionChecks()) {
                buttonText = EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + buttonText;
            }

            this.componentList.add(new QuickplayGuiButton(buttonKey, buttonId, this.width / 2 - this.buttonWidth / 2,
                    this.topOfButtons + (this.buttonHeight + this.buttonYMargins) * buttonId, this.buttonWidth, this.buttonHeight,
                    buttonText, true));
            buttonId++;
        }

        // Add launch, "All On" and "All Off" buttons
        this.componentList.add(new QuickplayGuiButton(null, -1, this.width / 2  - this.topButtonWidth / 2, 10,
                this.topButtonWidth, this.buttonHeight, Quickplay.INSTANCE.translator.get("quickplay.gui.party.launch"),
                false)); // Launch
        this.componentList.add(new QuickplayGuiButton(null, -2,
                this.width / 2 - this.topButtonWidth / 2 - this.topButtonWidth - this.topButtonMargins,
                10, this.topButtonWidth, 20, Quickplay.INSTANCE.translator.get("quickplay.gui.party.allon"),
                false)); // All on
        this.componentList.add(new QuickplayGuiButton(null, -3,
                this.width / 2 + this.topButtonWidth / 2 + this.topButtonMargins, 10, this.topButtonWidth, 20,
                Quickplay.INSTANCE.translator.get("quickplay.gui.party.alloff"), false)); // All off

    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        this.scrollFrameTop = this.topOfButtons;

        // Increase scroll speed & amount
        this.scrollMultiplier = 3;
        this.scrollDelay = 2;
        this.scrollbarYMargins = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        this.drawDefaultBackground();
        this.updateOpacity();

        if(Quickplay.INSTANCE.isEnabled) {
            drawScrollbar(this.width / 2 + this.buttonWidth / 2 + 3);

            //Override super.drawScreen(mouseX, mouseY, partialTicks);
            for (QuickplayGuiComponent component : this.componentList) {
                double scrollOpacity = component.scrollable ? ((component.y - this.scrollPixel) > this.topOfButtons ? 1 :
                        (component.y - this.scrollPixel) + this.scrollFadeDistance < this.topOfButtons ? 0 :
                                (this.scrollFadeDistance - ((double) this.topOfButtons - (double) (component.y - this.scrollPixel))) /
                                        (double) this.scrollFadeDistance) : 1;
                component.draw(this, mouseX, mouseY, this.opacity * scrollOpacity);
            }
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(this.fontRendererObj,
                    Quickplay.INSTANCE.translator.get("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.width / 2, this.height / 2, 0xffffff);
        }

        // Draw hovering text if the user's hovering over a button which is not usable in the current location
        for(QuickplayGuiComponent component : this.componentList) {
            // For the sake of simplicity we can just check if a string begins with the gray & italic formatting codes
            // to determine if a button is "disabled" in the users current location.
            if(component instanceof QuickplayGuiButton && component.mouseHovering(this, mouseX, mouseY) &&
                    component.displayString.startsWith(EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC)) {
                this.drawHoveringText(Collections.singletonList(
                        Quickplay.INSTANCE.translator.get("quickplay.party.thisModeNotAvailable")), mouseX, mouseY);
                break;
            }
        }


        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof String) {
            if(component.y - this.scrollPixel > this.topOfButtons - this.scrollFadeDistance && component.scrollable) {
                // Display name of this component, split at the colon, to separate the name from the current toggle status
                final String nameWithoutToggleStatus = component.displayString.split(":")[0];

                final boolean isEnabled = this.enabledButtons.contains(component.origin);

                // If the mode is toggled on remove it, otherwise add it
                if(isEnabled) {
                    this.enabledButtons.remove(component.origin);
                    component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.RED +
                            Quickplay.INSTANCE.translator.get("quickplay.config.gui.false");
                } else {
                    this.enabledButtons.add((String) component.origin);
                    component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.GREEN +
                            Quickplay.INSTANCE.translator.get("quickplay.config.gui.true");
                }
            }
        } else if(component.id == -3) {
            // Disable all
            this.enabledButtons.clear();
            this.componentList.clear();
            this.addButtons();
        } else if(component.id == -2) {
            // Enable all
            this.enabledButtons.addAll(this.allButtons);
            this.componentList.clear();
            this.addButtons();
        } else if(component.id == -1) {
            // Launch!
            Minecraft.getMinecraft().displayGuiScreen(null);
            Quickplay.INSTANCE.threadPool.submit(Quickplay.INSTANCE::launchPartyMode);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // If everything is enabled, assume the default is wanted. We set enabledButtonsForPartyMode to null so future
        // buttons added are enabled by default.
        if(this.enabledButtons == null || this.enabledButtons.containsAll(this.allButtons)) {
            Quickplay.INSTANCE.settings.enabledButtonsForPartyMode = null;
        }
        // You cannot disable all buttons, one button must always be enabled.
        else if(this.enabledButtons.size() < 1) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation("quickplay.party.mustSelectOneGame")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD))));
            return;
        } else {
            Quickplay.INSTANCE.settings.enabledButtonsForPartyMode = this.enabledButtons;
        }
        try {
            Quickplay.INSTANCE.settings.save();
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation("quickplay.config.saveError")));
        }
    }
}
