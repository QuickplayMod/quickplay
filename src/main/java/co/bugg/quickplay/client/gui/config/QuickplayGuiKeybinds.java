package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.*;
import co.bugg.quickplay.config.ConfigKeybinds;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Quickplay GUI for editing the list of keybinds known to the basic Quickplay settings
 */
public class QuickplayGuiKeybinds extends QuickplayGui {

    /**
     * Y position that the buttons for each keybind start at
     */
    public int topOfButtons;
    /**
     * The width of each button in the GUI
     */
    public int buttonWidth = 200;
    /**
     * The height of each button in the GUI
     */
    public int buttonHeight = 20;
    /**
     * The margins between each button on the GUI
     */
    public int buttonMargins = 3;
    /**
     * How wide the "Reset" button on the screen is
     */
    public int resetButtonWidth = 90;
    /**
     * The display text of the reset button
     */
    public final String resetButtonText = I18n.format("quickplay.keybinds.reset");
    /**
     * The color of keybinds on buttons when they are not being edited
     */
    public final EnumChatFormatting keybindColor = EnumChatFormatting.YELLOW;
    /**
     * The color of keybinds when the keybind is currently selected & being edited
     */
    public final EnumChatFormatting keybindEditingColor = EnumChatFormatting.GOLD;
    /**
     * The separating characters between a keybind's name and the key it's mapped to
     */
    public final String keybindNameSeparator = " : ";
    /**
     * Characters that are prepended to the keybind button display string when it is being edited
     */
    public final String keybindPrependedEditingText = "> ";
    /**
     * Characters that are appended to the keybind button display string when it is being edited
     */
    public final String keybindAppendedEditingText = " <";
    /**
     * The GUI component for the keybind that is currently selected and being edited
     */
    public QuickplayGuiComponent selectedComponent = null;
    /**
     * Whether a popup telling the client that the key they tried to assign is already taken
     * This is set to true whenever the user tries to bind a key that's already set to something else
     * It disappears shortly afterwards by setting this back to false.
     */
    public boolean drawTakenPopup;

    @Override
    public void initGui() {
        super.initGui();

        topOfButtons = (int) (height * 0.1);

        int buttonId = 0;

        // Header
        componentList.add(new QuickplayGuiString(null, buttonId, width / 2, topOfButtons + (buttonHeight + buttonMargins) * buttonId++, buttonWidth, buttonHeight, I18n.format("quickplay.keybinds.title"), true, true));

        for(QuickplayKeybind keybind : Quickplay.INSTANCE.keybinds.keybinds) {
            final QuickplayGuiComponent component = new QuickplayGuiButton(keybind, buttonId, width / 2 - buttonWidth / 2, topOfButtons + (buttonHeight + buttonMargins) * buttonId++, buttonWidth, buttonHeight, keybind.name, true);
            formatComponentString(component, false);
            componentList.add(component);
        }

        // Reset button
        componentList.add(new QuickplayGuiButton(null, buttonId, width - buttonMargins - resetButtonWidth, height - buttonMargins - buttonHeight, resetButtonWidth, buttonHeight, resetButtonText, false));

        setScrollingValues();
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        // TODO there's a weird bug here that causes items to fall off the screen on large screens. Resolved it temporarily by increasing scrollContentMargins but that can make things look silly depending on screen size
        scrollContentMargins = (int) (height * 0.15);
        // Apply this change by recalculating scroll height
        scrollContentHeight = calcScrollHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(drawTakenPopup) {
            final List<String> hoverText = new ArrayList<>();
            hoverText.add(I18n.format("quickplay.gui.keybinds.taken"));
            drawHoveringText(hoverText, mouseX, mouseY);
        }

        drawScrollbar(width / 2 + buttonWidth / 2 + 3);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for(QuickplayGuiComponent component : componentList) {
            if(mouseButton == 1 && component.origin instanceof QuickplayKeybind && component.mouseHovering(this, mouseX, mouseY)) {
                //noinspection ArraysAsListWithZeroOrOneArgument
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(I18n.format("quickplay.gui.keybinds.delete")), component, -1, mouseX, mouseY) {
                    @Override
                    public void optionSelected(int index) {
                        switch(index) {
                            case 0:
                                Quickplay.INSTANCE.keybinds.keybinds.remove(component.origin);
                                Quickplay.INSTANCE.unregisterEventHandler(component.origin);
                                try {
                                    Quickplay.INSTANCE.keybinds.save();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Quickplay.INSTANCE.sendExceptionRequest(e);
                                }

                                initGui();
                                break;
                        }
                    }
                };
                componentList.add(contextMenu);
                break;
            }
        }
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof QuickplayKeybind) {
            if(selectedComponent != null)
                formatComponentString(selectedComponent, false);
            selectedComponent = component;
            formatComponentString(component, true);
        } else if(component.displayString.equals(resetButtonText)) {
            try {
                // Unsubscribe all keybinds
                for(QuickplayKeybind keybind : Quickplay.INSTANCE.keybinds.keybinds)
                    Quickplay.INSTANCE.unregisterEventHandler(keybind);

                // Create a new keybind list
                Quickplay.INSTANCE.keybinds = new ConfigKeybinds(true);
                Quickplay.INSTANCE.keybinds.save();
                initGui();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        closeContextMenu();
        if(selectedComponent != null) {
            final QuickplayKeybind keybind = (QuickplayKeybind) selectedComponent.origin;
            if(Quickplay.INSTANCE.keybinds.keybinds.stream().anyMatch(keybind1 -> keybind1.key == keyCode && keybind != keybind1)) {
                // Key is already taken so cancel, draw a popup telling them, and hide it in 3 seconds
                drawTakenPopup = true;
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    drawTakenPopup = false;
                });
            } else {
                switch (keyCode) {
                    case Keyboard.KEY_ESCAPE:
                        keybind.key = Keyboard.KEY_NONE;
                        break;
                    default:
                        keybind.key = keyCode;
                        // Send analytical data to Google
                        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                            Quickplay.INSTANCE.threadPool.submit(() -> {
                                try {
                                    Quickplay.INSTANCE.ga.createEvent("Keybinds", "Keybind Changed")
                                            .setEventLabel(keybind.name + " : " + keybind.key)
                                            .send();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        break;
                }
            }

            try {
                formatComponentString(selectedComponent, false);
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
            selectedComponent = null;
            Quickplay.INSTANCE.keybinds.save();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Format the display string for the given component & keybind
     * @param component Component to format
     * @param selected Whether this component is currently selected/being edited or not
     * @throws IllegalArgumentException when the component provided's origin isn't a QuickplayKeybind
     */
    public void formatComponentString(QuickplayGuiComponent component, boolean selected) {
        if(component.origin instanceof QuickplayKeybind) {
            final QuickplayKeybind keybind = (QuickplayKeybind) component.origin;
            if(selected)
                component.displayString = keybindPrependedEditingText + keybind.name + keybindNameSeparator + keybindEditingColor + Keyboard.getKeyName(keybind.key) + EnumChatFormatting.RESET + keybindAppendedEditingText;
            else
                component.displayString = keybind.name + keybindNameSeparator + keybindColor + Keyboard.getKeyName(keybind.key) + EnumChatFormatting.RESET;
        } else
            throw new IllegalArgumentException("The GUI component provided does not have a QuickplayKeybind as it's origin!");
    }
}
