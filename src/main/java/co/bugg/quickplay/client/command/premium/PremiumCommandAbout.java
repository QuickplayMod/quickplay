package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays information about what Quickplay Premium is.
 * Quickplay attempts to grab this information from the web server on startup
 * If it doesn't exist, an error is sent.
 */
public class PremiumCommandAbout implements IPremiumCommand {
    @Override
    public String getName() {
        return "about";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Retrieve information about Quickplay Premium.";
    }

    @Override
    public void run(String[] args) {
        if (Quickplay.INSTANCE.premiumAbout != null) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(Quickplay.INSTANCE.premiumAbout, true, false));
        } else {
            final String premiumLink = "https://bugg.co/quickplay/premium";

            final ITextComponent chatComponent = new TextComponentTranslation("quickplay.commands.quickplay.premium.about.menuMissing", premiumLink).setStyle(new Style().setColor(TextFormatting.RED));
            final Style Style = new Style().setColor(TextFormatting.RED);
            Style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("quickplay.commands.quickplay.premium.about.menuMissing.clickToOpen", premiumLink).setStyle(new Style().setColor(TextFormatting.GRAY))));
            Style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, premiumLink));
            chatComponent.setStyle(Style);

            Quickplay.INSTANCE.messageBuffer.push(new Message(TextComponent, true, false));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
