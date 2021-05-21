package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.*;

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
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(Quickplay.INSTANCE.premiumAbout, true,
                    false));
        } else {
            final String premiumLink = "https://bugg.co/quickplay/premium";

            final ChatStyleWrapper redChatStyle = new ChatStyleWrapper().apply(Formatting.RED);
            final IChatComponentWrapper chatComponent = new QuickplayChatComponentTranslation(
                    "quickplay.commands.quickplay.premium.about.menuMissing", premiumLink)
                    .setStyle(redChatStyle);

            final ChatStyleWrapper clickChatStyle = new ChatStyleWrapper().apply(Formatting.RED);
            clickChatStyle.setHoverEvent(new HoverEventWrapper(HoverEventWrapper.Action.SHOW_TEXT,
                    new QuickplayChatComponentTranslation(
                    "quickplay.commands.quickplay.premium.about.menuMissing.clickToOpen", premiumLink)
                    .setStyle(new ChatStyleWrapper().apply(Formatting.GRAY))));

            clickChatStyle.setClickEvent(new ClickEventWrapper(ClickEventWrapper.Action.OPEN_URL, premiumLink));
            chatComponent.setStyle(clickChatStyle);

            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(chatComponent, true, false));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
