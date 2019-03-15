package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.config.ConfigUsageStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * GUI prompting the user if they'd like to send usage statistics
 * in order to help improve Quickplay.
 */
public class QuickplayGuiUsageStats extends QuickplayGui {

    /**
     * Width of each button on the screen
     */
    private static final int buttonWidth = 150;
    /**
     * Height of each button on the screen
     */
    private static final int buttonHeight = 20;
    /**
     * Margins between each button on the screen
     */
    private static final int buttonMargins = 5;
    /**
     * Usage stats configuration that is being altered
     */
    public final ConfigUsageStats usageStats = Quickplay.INSTANCE.usageStats;
    /**
     * Display string for the "YES" button
     */
    private final String yesText = I18n.format("quickplay.gui.stats.yes");
    /**
     * Display string for the "NO" button
     */
    private final String noText = I18n.format("quickplay.gui.stats.no");
    /**
     * String displaying the user's current Quickplay statistics token
     */
    private String tokenText = null;
    /**
     * String for the user to click to visit the privacy policy
     */
    private String privacyText = I18n.format("quickplay.gui.stats.privacy");
    /**
     * The lines that need to be rendered for the description on how Quickplay collects data & such
     */
    private String[] descriptionLines;

    @Override
    public void initGui() {
        super.initGui();
        int buttonY = (int) (height * 0.8);
        componentList.add(new QuickplayGuiButton(usageStats, 0, width / 2 - buttonWidth - buttonMargins / 2, buttonY, buttonWidth, buttonHeight, yesText, true));
        componentList.add(new QuickplayGuiButton(usageStats, 1, width / 2 + buttonMargins / 2, buttonY, buttonWidth, buttonHeight, noText, true));


        // Draw the stats token if it's available
        if (Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null) {
            tokenText = I18n.format("quickplay.gui.stats.token", Quickplay.INSTANCE.usageStats.statsToken.toString());
            componentList.add(new QuickplayGuiString(Quickplay.INSTANCE.usageStats.statsToken, 2, width / 2, buttonY - fontRendererObj.FONT_HEIGHT - 3, fontRendererObj.getStringWidth(tokenText), fontRendererObj.FONT_HEIGHT, tokenText, true, true));
        }
        componentList.add(new QuickplayGuiString("https://bugg.co/quickplay/privacy", 3, width / 2, buttonY - (fontRendererObj.FONT_HEIGHT + 3) * 2, fontRendererObj.getStringWidth(privacyText), fontRendererObj.FONT_HEIGHT, privacyText, true, true));

        int descriptionWidth = (int) (width * 0.8);
        final String description = I18n.format("quickplay.gui.stats.description");
        descriptionLines = fontRendererObj.listFormattedStringToWidth(description, descriptionWidth).toArray(new String[0]);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        if (opacity > 0) {
            final int headerY = (int) (height * 0.1);
            drawCenteredString(fontRendererObj, I18n.format("quickplay.gui.stats.title"), width / 2, headerY, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

            int lineHeight = headerY + fontRendererObj.FONT_HEIGHT + 5;
            for (String line : descriptionLines) {
                drawCenteredString(fontRendererObj, line, width / 2, lineHeight, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                lineHeight += fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // If hovering over the token button
        Optional<QuickplayGuiComponent> filteredStream = componentList.stream().filter(component -> component.displayString.equals(tokenText)).findFirst();
        if (tokenText != null && filteredStream.isPresent() && filteredStream.get().mouseHovering(this, mouseX, mouseY)) {
            drawHoveringText(Collections.singletonList(I18n.format("quickplay.gui.stats.copy")), mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if (privacyText != null && privacyText.equals(component.displayString)) {
            try {
                Desktop.getDesktop().browse(new URI((String) component.origin));
            } catch (IOException | URISyntaxException | ClassCastException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                // If origin isn't string for some reason, just put "contact bugfroggy" instead of a url.
                final String url = (component.origin instanceof String) ? "Visit " + component.origin : "Contact @bugfroggy.";
                component.displayString = I18n.format("quickplay.gui.stats.privacyerror", url);
            }
            // If the copy to clipboard text is clicked
        } else if (tokenText != null && tokenText.equals(component.displayString)) {
            final UUID token = (UUID) component.origin;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(token.toString()), null);
            component.displayString = I18n.format("quickplay.gui.stats.copied");
        } else {
            Quickplay.INSTANCE.promptUserForUsageStats = false;
            Quickplay.INSTANCE.usageStats = new ConfigUsageStats();
            Quickplay.INSTANCE.usageStats.sendUsageStats = component.displayString.equals(yesText);

            // Create a new Google Analytics instance in case it wasn't set before
            Quickplay.INSTANCE.createGoogleAnalytics();

            // Report the user's decision. This is one of the few things that is reported regardless of decision
            if (Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Privacy", "Privacy Settings Changed")
                                .setEventLabel("Report usage: " + Quickplay.INSTANCE.usageStats.sendUsageStats)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            try {
                Quickplay.INSTANCE.usageStats.save();
            } catch (IOException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    public void mouseScrolled(int distance) {
        // This GUI does not have scrolling capability
    }
}
