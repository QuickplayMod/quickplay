package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.client.gui.*;
import co.bugg.quickplay.config.ConfigKeybinds;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickplayGuiKeybinds extends QuickplayGui {

    public int topOfButtons = 0;
    public int buttonWidth = 200;
    public int buttonHeight = 20;
    public int buttonMargins = 3;
    public int resetButtonWidth = 90;

    public final String resetButtonText = new ChatComponentTranslation("quickplay.keybinds.reset").getUnformattedText();

    public final EnumChatFormatting keybindColor = EnumChatFormatting.YELLOW;
    public final EnumChatFormatting keybindEditingColor = EnumChatFormatting.GOLD;
    public final String keybindNameSeparator = " : ";
    public final String keybindPrependedEditingText = "> ";
    public final String keybindAppendedEditingText = " <";

    public QuickplayGuiComponent selectedComponent = null;

    public boolean drawTakenPopup;

    @Override
    public void initGui() {
        super.initGui();

        topOfButtons = (int) (height * 0.1);

        int buttonId = 0;

        // Header
        componentList.add(new QuickplayGuiString(null, buttonId, width / 2, topOfButtons + (buttonHeight + buttonMargins) * buttonId++, buttonWidth, buttonHeight, new ChatComponentTranslation("quickplay.keybinds.title").getUnformattedText(), true, true));

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
            hoverText.add(new ChatComponentTranslation("quickplay.gui.keybinds.taken").getUnformattedText());
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
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(new ChatComponentTranslation("quickplay.gui.keybinds.delete").getUnformattedText()), component, -1, mouseX, mouseY, false) {
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
                        break;
                }
            }

            formatComponentString(selectedComponent, false);
            selectedComponent = null;
            Quickplay.INSTANCE.keybinds.save();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public void formatComponentString(QuickplayGuiComponent component, boolean selected) {
        final QuickplayKeybind keybind = (QuickplayKeybind) component.origin;
        if(selected)
            component.displayString = keybindPrependedEditingText + keybind.name + keybindNameSeparator + keybindEditingColor + Keyboard.getKeyName(keybind.key) + EnumChatFormatting.RESET + keybindAppendedEditingText;
        else
            component.displayString = keybind.name + keybindNameSeparator + keybindColor + Keyboard.getKeyName(keybind.key) + EnumChatFormatting.RESET;
    }
}
