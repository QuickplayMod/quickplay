package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to retrieve a link to the Quickplay Discord
 */
public class SubCommandDiscord extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDiscord(ACommand parent) {
        super(
                parent,
                "discord",
                new ChatComponentTranslation("quickplay.commands.quickplay.discord.help").getUnformattedText(),
                "",
                true,
                true,
                90
        );
    }

    @Override
    public void run(String[] args) {
        final String link = "https://bugg.co/quickplay/discord";

        final IChatComponent chatComponent = new ChatComponentTranslation("quickplay.commands.quickplay.discord.message");
        chatComponent.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
        final IChatComponent linkComponent = new ChatComponentText(link);
        linkComponent.setChatStyle(new ChatStyle()
                .setColor(EnumChatFormatting.AQUA)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentTranslation("quickplay.chat.clickToOpen")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))
                        .appendSibling(new ChatComponentText(" " + link)
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)
                            )
                        )
                )
                )
        );

        final IChatComponent finalComponent = new ChatComponentText("").appendSibling(chatComponent).appendText("\n").appendSibling(linkComponent);
        Quickplay.INSTANCE.messageBuffer.push(new Message(finalComponent, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
