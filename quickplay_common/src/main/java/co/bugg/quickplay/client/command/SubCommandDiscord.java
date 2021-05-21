package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to retrieve a link to the Quickplay Discord
 */
public class SubCommandDiscord extends ACommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDiscord(ACommand parent) {
        super(
                parent,
                Collections.singletonList("discord"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.discord.help"),
                "",
                true,
                true,
                90,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        final String link = "https://bugg.co/quickplay/discord";

        final IChatComponentWrapper chatComponent = new QuickplayChatComponentTranslation("quickplay.commands.quickplay.discord.message");
        chatComponent.setStyle(new ChatStyleWrapper().apply(Formatting.YELLOW));
        final IChatComponentWrapper linkComponent = new ChatComponentTextWrapper(link);
        linkComponent.setStyle(new ChatStyleWrapper()
                .apply(Formatting.AQUA)
                .setClickEvent(new ClickEventWrapper(ClickEventWrapper.Action.OPEN_URL, link))
                .setHoverEvent(new HoverEventWrapper(HoverEventWrapper.Action.SHOW_TEXT,
                        new QuickplayChatComponentTranslation("quickplay.chat.clickToOpen")
                        .setStyle(new ChatStyleWrapper().apply(Formatting.GRAY))
                        .appendSibling(new ChatComponentTextWrapper(" " + link)
                            .setStyle(new ChatStyleWrapper().apply(Formatting.AQUA)
                            )
                        )
                )
                )
        );

        final IChatComponentWrapper finalComponent = new ChatComponentTextWrapper("")
                .appendSibling(chatComponent).appendText("\n").appendSibling(linkComponent);
        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(finalComponent, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
