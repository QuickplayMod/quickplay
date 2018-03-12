package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.games.PartyMode;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.Random;
import java.util.concurrent.Future;

/**
 * Spinner GUI for determining a party mode & sending the client to it
 */
public class QuickplayGuiPartySpinner extends QuickplayGui {

    /**
     * How large {@link #spinnerText} should be rendered
     */
    public final double spinnerScale = 1.5;
    /**
     * The display string for the spinner
     */
    public String spinnerText;
    /**
     * How long the spinner should spin for
     */
    public final double spinnerDelay = Quickplay.INSTANCE.settings.partyModeDelay;
    /**
     * Mode currently selected by the spinner & in view to the user
     */
    public PartyMode currentlySelectedMode;
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

        // If there are any modes
        if(Quickplay.INSTANCE.settings.partyModes.size() > 0) {
            randomizingTextHeight = (int) (height * (height > 350 ? 0.4 : 0.3));

            int buttonId = 0;

            final String randomizingString = I18n.format("quickplay.gui.party.randomizing");
            componentList.add(new QuickplayGuiString(null, buttonId++, width / 2, randomizingTextHeight, fontRendererObj.getStringWidth(randomizingString), fontRendererObj.FONT_HEIGHT, randomizingString, true, false));

            if (spinningThreadFuture == null)
                startSpinner();
        } else {
            // close the GUI and send an error
            Minecraft.getMinecraft().displayGuiScreen(null);
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.party.nogames").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    /**
     * Start the spinner, spinning for a mode to join, and then finish it off by flashing that mode & then joining it
     */
    public void startSpinner() {
        final long startedAt = System.currentTimeMillis();
        final Random random = new Random();

        spinningThreadFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                if(spinnerDelay > 0) {
                    // While less than 100% of the spinnerDelay has passed
                    while (startedAt > System.currentTimeMillis() - spinnerDelay * 1000) {
                        final int nextSelectedModeIndex = random.nextInt(Quickplay.INSTANCE.settings.partyModes.size());
                        currentlySelectedMode = Quickplay.INSTANCE.settings.partyModes.get(nextSelectedModeIndex);
                        spinnerText = currentlySelectedMode.name;

                        // Sleep for 1/5th of the length this spinner has been running
                        // This creates a fast spinning speed to start that slows down over time
                        // Minimum of 10ms and max of 1000ms
                        long sleepTime = (long) ((1 / 5d) * (System.currentTimeMillis() - startedAt));
                        if (sleepTime < 10) sleepTime = 10;
                        if (sleepTime > 1000) sleepTime = 1000;

                        Thread.sleep(sleepTime);
                    }
                } else {
                    final int nextSelectedModeIndex = random.nextInt(Quickplay.INSTANCE.settings.partyModes.size());
                    currentlySelectedMode = Quickplay.INSTANCE.settings.partyModes.get(nextSelectedModeIndex);
                    spinnerText = currentlySelectedMode.name;
                }

                // After spinning complete, start finalization
                final String textToFlash = spinnerText;
                // While less than 100% of the spinnerDelay and finalization period combined has passed
                while (startedAt > System.currentTimeMillis() - (spinnerDelay + finalizationLength) * 1000) {
                    if (spinnerText.equals(textToFlash)) {
                        spinnerText = "";
                    } else
                        spinnerText = textToFlash;

                    Thread.sleep((long) (flashFrequency * 1000));
                }

                // Finally, if this GUI is still open, send the mode command & close GUI
                if(Minecraft.getMinecraft().currentScreen == this) {
                    Quickplay.INSTANCE.chatBuffer.push(currentlySelectedMode.command);
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        // draw background box
        drawRect(0, randomizingTextHeight - boxPadding, width, (int) (randomizingTextHeight + fontRendererObj.FONT_HEIGHT * spinnerScale * 3 + boxPadding), (int) (opacity * 255 * 0.5) << 24);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw spinner
        GL11.glScaled(spinnerScale, spinnerScale, spinnerScale);
        drawCenteredString(fontRendererObj, spinnerText, (int) (width / 2 / spinnerScale), (int) ((randomizingTextHeight + fontRendererObj.FONT_HEIGHT * spinnerScale * 2) / spinnerScale), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        GL11.glScaled(1 / spinnerScale, 1 / spinnerScale, 1 / spinnerScale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
