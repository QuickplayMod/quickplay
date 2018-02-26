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
import java.util.Arrays;

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

    @Override
    public void initGui() {
        super.initGui();

        topOfButtons = (int) (height * 0.1);

        int buttonId = 0;

        // Header
        componentList.add(new QuickplayGuiHeader(null, buttonId, width / 2, topOfButtons + (buttonHeight + buttonMargins) * buttonId++, buttonWidth, buttonHeight, new ChatComponentTranslation("quickplay.keybinds.title").getUnformattedText()));

        for(QuickplayKeybind keybind : Quickplay.INSTANCE.keybinds.keybinds) {
            final QuickplayGuiComponent component = new QuickplayGuiButton(keybind, buttonId, width / 2 - buttonWidth / 2, topOfButtons + (buttonHeight + buttonMargins) * buttonId++, buttonWidth, buttonHeight, keybind.name);
            formatComponentString(component, false);
            componentList.add(component);
        }

        // Reset button
        componentList.add(new QuickplayGuiButton(null, buttonId, width - buttonMargins - resetButtonWidth, height - buttonMargins - buttonHeight, resetButtonWidth, buttonHeight, resetButtonText));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(QuickplayGuiComponent component : componentList) {
            if(mouseButton == 1 && component.origin instanceof QuickplayKeybind && component.mouseHovering(mc, mouseX, mouseY)) {
                //noinspection ArraysAsListWithZeroOrOneArgument
                contextMenu = new QuickplayGuiContextMenu(Arrays.asList(new ChatComponentTranslation("quickplay.gui.keybinds.delete").getUnformattedText()), component, -1, mouseX, mouseY) {
                    @Override
                    public void optionSelected(int index) {
                        switch(index) {
                            case 0:
                                Quickplay.INSTANCE.keybinds.keybinds.remove(component.origin);
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
            switch(keyCode) {
                case Keyboard.KEY_ESCAPE:
                    keybind.key = Keyboard.KEY_NONE;
                    break;
                default:
                    keybind.key = keyCode;
                    break;
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

    @Override
    public void mouseScrolled(int distance) {

        // Scroll is animated, one pixel per 1ms
        Quickplay.INSTANCE.threadPool.submit(() -> {

            // Figure out which component is the highest on screen & which is lowest
            QuickplayGuiComponent lowestComponent = null;
            QuickplayGuiComponent highestComponent = null;
            for(QuickplayGuiComponent component : componentList) {
                if(lowestComponent == null || lowestComponent.y < component.y)
                    lowestComponent = component;
                if(highestComponent == null || highestComponent.y > component.y)
                    highestComponent = component;
            }

            if(componentList.size() > 0)
                // Quick scrolling is important in this GUI so scroll speed * distance increased
                for (int i = 0; i < Math.abs(distance); i++) {

                    // Only allow scrolling if there is an element off screen
                    // If scrolling down & the last element is at all off the screen (plus the additional margins for aesthetic purposes)
                    if((distance < 0 && lowestComponent.y > height - buttonHeight - buttonMargins) ||
                            // OR if scrolling up & the top element is currently at all off of the screen
                            (distance > 0 && highestComponent.y < topOfButtons)) {

                        for (QuickplayGuiComponent component : componentList) {
                            if(!component.displayString.equals(resetButtonText))
                                component.move(distance < 0 ? -1 : 1);
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        // Already reached the bottom/top, so stop trying to scroll
                        break;
                    }
                }
        });
    }
}
