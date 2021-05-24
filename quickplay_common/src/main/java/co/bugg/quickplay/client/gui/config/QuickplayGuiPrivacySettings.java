package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.wrappers.chat.Formatting;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class QuickplayGuiPrivacySettings extends QuickplayGui {

    @Override
    public void hookInit() {
        super.hookInit();

        float scale = 2.0f;
        int currentObjHeight;
        if(this.getHeight() < 450) {
            currentObjHeight = 50;
        } else {
            currentObjHeight = this.getHeight() / 3;
        }
        int currentId = 0;

        int contentWidth = (int) Math.min(this.getWidth() * 0.8, 500);

        final String trueStr = Quickplay.INSTANCE.elementController.translate("quickplay.config.gui.true");
        final String falseStr = Quickplay.INSTANCE.elementController.translate("quickplay.config.gui.false");

        // TITLE

        this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                contentWidth, this.getFontHeight(), Quickplay.INSTANCE.elementController.translate("quickplay.privacy"),
                true, true, false, scale));
        currentObjHeight += (this.getFontHeight() * scale) + 5;

        this.componentList.add(new QuickplayGuiString("PRIVACY", currentId++, this.getWidth() / 2, currentObjHeight,
                contentWidth, this.getFontHeight(), Quickplay.INSTANCE.elementController.translate("quickplay.privacy.policy"),
                true, true, true));
        currentObjHeight += this.getFontHeight() + 20;

        // ANONYMOUS MODE
        scale = 1.5f;

        this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                contentWidth, this.getFontHeight(), Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousMode"),
                true, true, false, scale));
        currentObjHeight += (this.getFontHeight() * scale) + 5;

        List<String> lines = this.listFormattedStringToWidth(Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousMode.about"),
                contentWidth);

        for(final String line : lines) {
            this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                    contentWidth, this.getFontHeight(), line,
                    true, true, true));
            currentObjHeight += this.getFontHeight() + 5;
        }

        String buttonStr = Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousMode") +
                ": " + (Quickplay.INSTANCE.settings.anonymousMode ? (Formatting.GREEN + trueStr) : (Formatting.RED + falseStr));
        int buttonWidth = 200;
        this.componentList.add(new QuickplayGuiButton("ANONYMOUS", currentId++, this.getWidth() / 2 - buttonWidth / 2,
                currentObjHeight,
                buttonWidth, 20, buttonStr, true));
        currentObjHeight += 50;

        // ANONYMOUS STATISTICS

        this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                contentWidth, this.getFontHeight(), Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousStats"),
                true, true, false, scale));
        currentObjHeight += (this.getFontHeight() * scale) + 5;

        lines = this.listFormattedStringToWidth(Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousStats.about"),
                contentWidth);

        for(final String line : lines) {
            this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                    contentWidth, this.getFontHeight(), line,
                    true, true, true));
            currentObjHeight += this.getFontHeight() + 5;
        }

        buttonStr = Quickplay.INSTANCE.elementController.translate("quickplay.privacy.anonymousStats") + ": " +
                (Quickplay.INSTANCE.settings.anonymousStatistics ? (Formatting.GREEN + trueStr) : (Formatting.RED + falseStr));
        this.componentList.add(new QuickplayGuiButton("STATISTICS", currentId++, this.getWidth() / 2 - buttonWidth / 2,
                currentObjHeight,
                buttonWidth, 20, buttonStr, true));
        currentObjHeight += 50;

        // "PRESS ESCAPE TO CLOSE"

        lines = this.listFormattedStringToWidth(Quickplay.INSTANCE.elementController.translate("quickplay.privacy.close"),
                contentWidth);

        for(final String line : lines) {
            this.componentList.add(new QuickplayGuiString(null, currentId++, this.getWidth() / 2, currentObjHeight,
                    contentWidth, this.getFontHeight(), line,
                    true, true, true));
            currentObjHeight += this.getFontHeight() + 5;
        }

        this.setScrollingValues();
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        if(this.getHeight() < 450) {
            this.scrollFrameTop = 50;
        } else {
            this.scrollFrameTop = this.getHeight() / 3;
        }
    }

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.hookRender(mouseX, mouseY, partialTicks);
        this.drawScrollbar(this.getWidth() - 5);
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);

        if(component.origin == null) {
            return;
        }

        if(component.origin.equals("ANONYMOUS")) {
            Quickplay.INSTANCE.settings.anonymousMode = !Quickplay.INSTANCE.settings.anonymousMode;
            component.displayString = "Anonymous Mode: " + (Quickplay.INSTANCE.settings.anonymousMode ?
                    (Formatting.GREEN + "TRUE") : (Formatting.RED + "FALSE"));
            Quickplay.INSTANCE.socket.reconnect();
        } else if(component.origin.equals("STATISTICS")) {
            Quickplay.INSTANCE.settings.anonymousStatistics = !Quickplay.INSTANCE.settings.anonymousStatistics;
            component.displayString = "Anonymous Statistics: " + (Quickplay.INSTANCE.settings.anonymousStatistics ?
                    (Formatting.GREEN + "TRUE") : (Formatting.RED + "FALSE"));
            Quickplay.INSTANCE.socket.reconnect();
        } else if(component.origin.equals("PRIVACY")) {
            try {
                Desktop.getDesktop().browse(new URI("https://bugg.co/quickplay/privacy/"));
            } catch (IOException | URISyntaxException e) {
                Quickplay.LOGGER.warning("Failed to open URL https://bugg.co/quickplay/privacy/");
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
        }
    }
}
