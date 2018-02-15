package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.QuickplayGuiHeader;
import co.bugg.quickplay.config.ConfigUsageStats;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class QuickplayGuiUsageStats extends QuickplayGui {

    public final int buttonWidth = 150;
    public final int buttonHeight = 20;
    public final int buttonMargins = 5;
    public int buttonY;
    public final ConfigUsageStats usageStats = Quickplay.INSTANCE.usageStats;

    public final String yesText = new ChatComponentTranslation("quickplay.gui.stats.yes").getUnformattedText();
    public final String noText = new ChatComponentTranslation("quickplay.gui.stats.no").getUnformattedText();
    public String tokenText = null;
    public String privacyText = new ChatComponentTranslation("quickplay.gui.stats.privacy").getFormattedText();

    public int descriptionWidth;
    public String[] descriptionLines;

    @Override
    public void initGui() {
        super.initGui();
        buttonY = (int) (height * 0.8);
        componentList.add(new QuickplayGuiButton(usageStats, 0, width / 2 - buttonWidth - buttonMargins / 2, buttonY, buttonWidth, buttonHeight, yesText));
        componentList.add(new QuickplayGuiButton(usageStats, 1, width / 2 + buttonMargins / 2, buttonY, buttonWidth, buttonHeight, noText));


        // Draw the stats token if it's available
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null) {
            tokenText = new ChatComponentTranslation("quickplay.gui.stats.token", Quickplay.INSTANCE.usageStats.statsToken.toString()).getUnformattedText();
            componentList.add(new QuickplayGuiHeader(Quickplay.INSTANCE.usageStats.statsToken, 2, width / 2, buttonY - fontRendererObj.FONT_HEIGHT - 3, fontRendererObj.getStringWidth(tokenText), fontRendererObj.FONT_HEIGHT, tokenText));
        }
        componentList.add(new QuickplayGuiHeader("https://bugg.co/quickplay/privacy", 3, width / 2, buttonY - (fontRendererObj.FONT_HEIGHT + 3) * 2, fontRendererObj.getStringWidth(privacyText), fontRendererObj.FONT_HEIGHT, privacyText));

        descriptionWidth = (int) (width * 0.8);
        final String description = new ChatComponentTranslation("quickplay.gui.stats.description").getUnformattedText();
        descriptionLines = fontRendererObj.listFormattedStringToWidth(description, descriptionWidth).toArray(new String[0]);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        final int headerY = (int) (height * 0.1);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("quickplay.gui.stats.title").getUnformattedText(), width / 2, headerY, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

        int lineHeight = headerY + fontRendererObj.FONT_HEIGHT + 5;
        for(String line : descriptionLines) {
            drawCenteredString(fontRendererObj, line, width / 2, lineHeight, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
            lineHeight += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // If hovering over the token button
        Optional<QuickplayGuiComponent> filteredStream = componentList.stream().filter(component -> component.displayString.equals(tokenText)).findFirst();
        if(tokenText != null && filteredStream.isPresent() && filteredStream.get().mouseHovering(mc, mouseX, mouseY)) {
            drawHoveringText(Collections.singletonList(new ChatComponentTranslation("quickplay.gui.stats.copy").getUnformattedText()), mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(privacyText != null && privacyText.equals(component.displayString)) {
            try {
                Desktop.getDesktop().browse(new URI((String) component.origin));
            } catch (IOException | URISyntaxException | ClassCastException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                // If origin isn't string for some reason, just put "contact bugfroggy" instead of a url.
                final String url = (component.origin instanceof String) ? "Visit " + component.origin : "Contact @bugfroggy.";
                component.displayString = new ChatComponentTranslation("quickplay.gui.stats.privacyerror", url).getUnformattedText();
            }
            // If the copy to clipboard text is clicked
        } else if(tokenText != null && tokenText.equals(component.displayString)) {
            final UUID token = (UUID) component.origin;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(token.toString()), null);
            component.displayString = new ChatComponentTranslation("quickplay.gui.stats.copied").getUnformattedText();
        } else {
            Quickplay.INSTANCE.promptUserForUsageStats = false;
            Quickplay.INSTANCE.usageStats = new ConfigUsageStats();
            Quickplay.INSTANCE.usageStats.sendUsageStats = component.displayString.equals(yesText);
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

    }
}
