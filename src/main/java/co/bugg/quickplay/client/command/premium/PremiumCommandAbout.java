package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays information about what Quickplay Premium is.
 * Quickplay attempts to grab this information from the web server on startup
 * If it doesn't exist, an error is sent.
 */
public class PremiumCommandAbout extends ACommand {

    public PremiumCommandAbout(ACommand parent) {
        super(
                parent,
                Collections.singletonList("about"),
                "Retrieve information about Quickplay Premium.",
                "",
                true,
                true,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if (Quickplay.INSTANCE.premiumAbout != null) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(Quickplay.INSTANCE.premiumAbout, true,
                    false));
        } else {
            final String premiumLink = "https://bugg.co/quickplay/premium";

            final ChatStyle redChatStyle = new ChatStyle().setColor(EnumChatFormatting.RED);
            final IChatComponent chatComponent = new ChatComponentTranslation(
                    "quickplay.commands.quickplay.premium.about.menuMissing", premiumLink)
                    .setChatStyle(redChatStyle);

            final ChatStyle clickChatStyle = new ChatStyle().setColor(EnumChatFormatting.RED);
            clickChatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentTranslation(
                    "quickplay.commands.quickplay.premium.about.menuMissing.clickToOpen", premiumLink)
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))));

            clickChatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, premiumLink));
            chatComponent.setChatStyle(clickChatStyle);

            Quickplay.INSTANCE.messageBuffer.push(new Message(chatComponent, true, false));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
