package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.config.ConfigUsageStats;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;

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
    public final int buttonWidth = 150;
    /**
     * Height of each button on the screen
     */
    public final int buttonHeight = 20;
    /**
     * Margins between each button on the screen
     */
    public final int buttonMargins = 5;
    /**
     * Y levels of the yes and no buttons
     */
    public int buttonY;
    /**
     * Usage stats configuration that is being altered
     */
    public final ConfigUsageStats usageStats = Quickplay.INSTANCE.usageStats;
    /**
     * Display string for the "YES" button
     */
    public final String yesText = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.yes");
    /**
     * Display string for the "NO" button
     */
    public final String noText = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.no");
    /**
     * String displaying the user's current Quickplay statistics token
     */
    public String tokenText = null;
    /**
     * String for the user to click to visit the privacy policy
     */
    public String privacyText = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.privacy");
    /**
     * The max width of the description text on what data Quickplay collects & such
     */
    public int descriptionWidth;
    /**
     * The lines that need to be rendered for the description on how Quickplay collects data & such
     */
    public String[] descriptionLines;

    @Override
    public void hookInit() {
        super.hookInit();
        this.buttonY = (int) (this.getHeight() * 0.8);
        this.componentList.add(new QuickplayGuiButton(this.usageStats, 0, this.getWidth() / 2 - this.buttonWidth - this.buttonMargins / 2,
                this.buttonY, this.buttonWidth, this.buttonHeight, this.yesText, true));
        this.componentList.add(new QuickplayGuiButton(this.usageStats, 1, this.getWidth() / 2 + this.buttonMargins / 2, this.buttonY,
                this.buttonWidth, this.buttonHeight, this.noText, true));


        // Draw the stats token if it's available
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null) {
            this.tokenText = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.token", Quickplay.INSTANCE.usageStats.statsToken.toString());
            this.componentList.add(new QuickplayGuiString(Quickplay.INSTANCE.usageStats.statsToken, 2, this.getWidth() / 2,
                    this.buttonY - this.getFontHeight() - 3, this.getStringWidth(tokenText),
                    this.getFontHeight(), this.tokenText, true, true));
        }
        this.componentList.add(new QuickplayGuiString("https://bugg.co/quickplay/privacy", 3, this.getWidth() / 2,
                this.buttonY - (this.getFontHeight() + 3) * 2, this.getStringWidth(this.privacyText),
                this.getFontHeight(), this.privacyText, true, true));

        this.descriptionWidth = (int) (this.getWidth() * 0.8);
        final String description = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.description");
        this.descriptionLines = this.listFormattedStringToWidth(description, this.descriptionWidth).toArray(new String[0]);
    }

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        this.drawDefaultBackground();

        if(Quickplay.INSTANCE.isEnabled) {
            if (this.opacity > 0) {
                final int headerY = (int) (this.getHeight() * 0.1);
                this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.title"), this.getWidth() / 2, headerY,
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);

                int lineHeight = headerY + this.getFontHeight() + 5;
                for (String line : this.descriptionLines) {
                    this.drawCenteredString(line, this.getWidth() / 2, lineHeight, Quickplay.INSTANCE.settings
                            .secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    lineHeight += this.getFontHeight();
                }
            }

            super.hookRender(mouseX, mouseY, partialTicks);

            // If hovering over the token button
            Optional<QuickplayGuiComponent> filteredStream = this.componentList.stream().filter(component ->
                    component.displayString.equals(this.tokenText)).findFirst();
            if (this.tokenText != null && filteredStream.isPresent() && filteredStream.get().isMouseHovering(this, mouseX, mouseY)) {
                this.drawHoveringText(Collections.singletonList(Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.copy")), mouseX, mouseY);
            }
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(Quickplay.INSTANCE.elementController
                            .translate("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.getWidth() / 2, this.getHeight() / 2, 0xffffff);
        }

        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(this.privacyText != null && this.privacyText.equals(component.displayString)) {
            try {
                Desktop.getDesktop().browse(new URI((String) component.origin));
            } catch (IOException | URISyntaxException | ClassCastException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                // If origin isn't string for some reason, just put "contact bugfroggy" instead of a url.
                final String url = (component.origin instanceof String) ? "Visit " + component.origin : "Contact @bugfroggy.";
                component.displayString = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.privacyerror", url);
            }
            // If the copy to clipboard text is clicked
        } else if(this.tokenText != null && this.tokenText.equals(component.displayString)) {
            final UUID token = (UUID) component.origin;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(token.toString()), null);
            component.displayString = Quickplay.INSTANCE.elementController.translate("quickplay.gui.stats.copied");
        } else {
            Quickplay.INSTANCE.promptUserForUsageStats = false;
            Quickplay.INSTANCE.usageStats = new ConfigUsageStats();
            Quickplay.INSTANCE.usageStats.sendUsageStats = component.displayString.equals(this.yesText);

            // Create a new Google Analytics instance in case it wasn't set before
            Quickplay.INSTANCE.createGoogleAnalytics();

            // Report the user's decision. This is one of the few things that is reported regardless of decision
            if(Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.ga != null) {
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
            Quickplay.INSTANCE.minecraft.openGui(null);
        }
    }

    @Override
    public void mouseScrolled(int distance) {
        // This GUI does not have scrolling capability
    }
}
