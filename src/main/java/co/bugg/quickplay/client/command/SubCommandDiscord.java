package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.discord.help"),
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

        final IChatComponent chatComponent = new QuickplayChatComponentTranslation("quickplay.commands.quickplay.discord.message");
        chatComponent.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
        final IChatComponent linkComponent = new ChatComponentText(link);
        linkComponent.setChatStyle(new ChatStyle()
                .setColor(EnumChatFormatting.AQUA)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new QuickplayChatComponentTranslation("quickplay.chat.clickToOpen")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))
                        .appendSibling(new ChatComponentText(" " + link)
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)
                            )
                        )
                )
                )
        );

        final IChatComponent finalComponent = new ChatComponentText("")
                .appendSibling(chatComponent).appendText("\n").appendSibling(linkComponent);
        Quickplay.INSTANCE.messageBuffer.push(new Message(finalComponent, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
