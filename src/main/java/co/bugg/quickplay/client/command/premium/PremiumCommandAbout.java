package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
            Quickplay.INSTANCE.messageBuffer.push(new Message(Quickplay.INSTANCE.premiumAbout, true));
        } else {
            final String premiumLink = "https://bugg.co/quickplay/premium";

            final IChatComponent chatComponent = new ChatComponentTranslation("quickplay.commands.quickplay.premium.about.menuMissing", premiumLink).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
            final ChatStyle chatStyle = new ChatStyle().setColor(EnumChatFormatting.RED);
            chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentTranslation("quickplay.commands.quickplay.premium.about.menuMissing.clickToOpen", premiumLink).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))));
            chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, premiumLink));
            chatComponent.setChatStyle(chatStyle);

            Quickplay.INSTANCE.messageBuffer.push(new Message(chatComponent, true));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
