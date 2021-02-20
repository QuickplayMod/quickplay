package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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
                I18n.format("quickplay.commands.quickplay.discord.help"),
                "",
                true,
                true,
                90
        );
    }

    @Override
    public void run(String[] args) {
        final String link = "https://bugg.co/quickplay/discord";

        final ITextComponent chatComponent = new TextComponentTranslation("quickplay.commands.quickplay.discord.message");
        chatComponent.setStyle(new Style().setColor(TextFormatting.YELLOW));
        final ITextComponent linkComponent = new TextComponentString(link);
        linkComponent.setStyle(new Style()
                .setColor(TextFormatting.AQUA)
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponentTranslation("quickplay.chat.clickToOpen")
                        .setStyle(new Style().setColor(TextFormatting.GRAY))
                        .appendSibling(new TextComponentString(" " + link)
                            .setStyle(new Style().setColor(TextFormatting.AQUA)
                            )
                        )
                )
                )
        );

        final ITextComponent finalComponent = new TextComponentString("")
                .appendSibling(chatComponent).appendText("\n").appendSibling(linkComponent);
        Quickplay.INSTANCE.messageBuffer.push(new Message(finalComponent, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
