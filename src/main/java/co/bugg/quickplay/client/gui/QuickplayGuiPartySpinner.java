package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Button;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Spinner GUI for determining a party mode & sending the client to it
 */
public class QuickplayGuiPartySpinner extends QuickplayGui {

    public Set<String> buttonSet;
    /**
     * How large {@link #currentButton} should be rendered
     */
    public final double spinnerScale = 1.5;
    /**
     * How long the spinner should spin for
     */
    public final double spinnerDelay = Quickplay.INSTANCE.settings.partyModeDelay;
    /**
     * Mode currently selected by the spinner & in view to the user
     */
    public String currentButton;
    /**
     * The current button's translated text to display.
     */
    public String currentButtonDisplayText;
    /**
     * Height at which the "Randomizing" text is rendered
     */
    public int randomizingTextHeight;
    /**
     * After spinning, how long the finalization screen should appear for before warping the user
     */
    public double finalizationLength = 4.0;
    /**
     * How quickly the spinnerText should flash during finalization
     */
    public double flashFrequency = 0.5;
    /**
     * Future thread containing the spinning action.
     * Used to avoid two spinners at once if initGui is called twice
     */
    public Future spinningThreadFuture;
    /**
     * Number of pixels on each side the background box has for padding
     */
    public int boxPadding = 20;

    @Override
    public void initGui() {
        super.initGui();

        if(Quickplay.INSTANCE.buttonMap == null) {
            // close the GUI and send an error
            Minecraft.getMinecraft().displayGuiScreen(null);
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation("quickplay.party.noGames")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }

        // Get the list of enabled games, unfiltered at the moment. If the user doesn't have an explicit list of
        // games selected, then the default of all games is used.
        if(Quickplay.INSTANCE.settings.enabledButtonsForPartyMode != null &&
                Quickplay.INSTANCE.settings.enabledButtonsForPartyMode.size() > 0) {
            this.buttonSet = new HashSet<>(Quickplay.INSTANCE.settings.enabledButtonsForPartyMode);
        } else {
            this.buttonSet = new HashSet<>(Quickplay.INSTANCE.buttonMap.keySet());
        }


        // Filter out buttons which are not found / don't pass permission checks / aren't visible to party mode
        for(String buttonKey : new HashSet<>(this.buttonSet)) {
            Button button = Quickplay.INSTANCE.buttonMap.get(buttonKey);
            if(button == null || !button.visibleInPartyMode || !button.passesPermissionChecks()) {
                this.buttonSet.remove(buttonKey);
            }
        }

        if(this.buttonSet.size() <= 0) {
            // close the GUI and send an error
            Minecraft.getMinecraft().displayGuiScreen(null);
            Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation("quickplay.party.noGames")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }

        this.randomizingTextHeight = (int) (this.height * (this.height > 350 ? 0.4 : 0.3));

        int buttonId = 0;
        final String randomizingString = Quickplay.INSTANCE.translator.get("quickplay.gui.party.randomizing");
        this.componentList.add(new QuickplayGuiString(null, buttonId++, this.width / 2, this.randomizingTextHeight,
                this.fontRendererObj.getStringWidth(randomizingString), this.fontRendererObj.FONT_HEIGHT, randomizingString,
                true, false));

        if (spinningThreadFuture == null) {
            startSpinner();
        }
    }

    /**
     * Start the spinner, spinning for a mode to join, and then finish it off by flashing that mode & then joining it
     */
    public void startSpinner() {
        final long startedAt = System.currentTimeMillis();
        final Random random = new Random();

        this.spinningThreadFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                if(this.spinnerDelay > 0) {
                    // While less than 100% of the spinnerDelay has passed
                    while (startedAt > System.currentTimeMillis() - this.spinnerDelay * 1000) {
                        if(Minecraft.getMinecraft().currentScreen == this) {
                            final int nextSelectedModeIndex = random.nextInt(this.buttonSet.size());
                            this.currentButton = this.buttonSet.stream().skip(nextSelectedModeIndex).findFirst().orElse(null);
                            this.currentButtonDisplayText = Quickplay.INSTANCE.translator
                                    .get(Quickplay.INSTANCE.buttonMap.get(this.currentButton).translationKey);

                            // Play sound
                            this.mc.thePlayer.playSound("liquid.lavapop", 1.0f, 2.0f);

                            // Sleep for 1/5th of the length this spinner has been running
                            // This creates a fast spinning speed to start that slows down over time
                            // Minimum of 30ms and max of 1000ms
                            long sleepTime = (long) ((1 / 5d) * (System.currentTimeMillis() - startedAt));
                            if (sleepTime < 30) sleepTime = 30;
                            if (sleepTime > 1000) sleepTime = 1000;

                            Thread.sleep(sleepTime);
                        } else {
                            return;
                        }
                    }
                } else {
                    final int nextSelectedModeIndex = random.nextInt(this.buttonSet.size());
                    this.currentButton = this.buttonSet.stream().skip(nextSelectedModeIndex).findFirst().orElse(null);
                    this.currentButtonDisplayText = Quickplay.INSTANCE.translator
                            .get(Quickplay.INSTANCE.buttonMap.get(this.currentButton).translationKey);
                }

                // After spinning complete, start finalization
                // Play dingy sound
                this.mc.thePlayer.playSound("random.levelup", 1.0f, 0.7f);
                final String textToFlash = this.currentButtonDisplayText;
                // While less than 100% of the spinnerDelay and finalization period combined has passed
                while (startedAt > System.currentTimeMillis() - (this.spinnerDelay + this.finalizationLength) * 1000) {
                    if(Minecraft.getMinecraft().currentScreen == this) {
                        if (this.currentButtonDisplayText.equals(textToFlash)) {
                            this.currentButtonDisplayText = "";
                        } else {
                            this.currentButtonDisplayText = textToFlash;
                        }

                        Thread.sleep((long) (this.flashFrequency * 1000));
                    } else {
                        return;
                    }
                }

                // Finally, if this GUI is still open, send the mode command & close GUI
                if(Minecraft.getMinecraft().currentScreen == this) {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                    Quickplay.INSTANCE.buttonMap.get(this.currentButton).run();
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        drawDefaultBackground();

        // draw background box
        drawRect(0, this.randomizingTextHeight - this.boxPadding, this.width,
                (int) (this.randomizingTextHeight + this.fontRendererObj.FONT_HEIGHT * this.spinnerScale * 3 + this.boxPadding),
                (int) (this.opacity * 255 * 0.5) << 24);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw spinner
        if(this.opacity > 0) {
            GlStateManager.scale(this.spinnerScale, this.spinnerScale, this.spinnerScale);
            drawCenteredString(this.fontRendererObj, this.currentButtonDisplayText, (int) (this.width / 2 / this.spinnerScale),
                    (int) ((this.randomizingTextHeight + this.fontRendererObj.FONT_HEIGHT * this.spinnerScale * 2) / this.spinnerScale),
                    Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
            GlStateManager.scale(1 / this.spinnerScale, 1 / this.spinnerScale, 1 / this.spinnerScale);
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
