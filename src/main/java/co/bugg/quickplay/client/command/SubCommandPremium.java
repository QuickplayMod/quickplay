package co.bugg.quickplay.client.command;

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
 * Sub command for the limbo command
 */
public class SubCommandPremium extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandPremium(ACommand parent) {
        super(
                parent,
                "premium",
                new ChatComponentTranslation("quickplay.commands.quickplay.premium.help").getUnformattedText(),
                "[help]",
                true,
                true,
                91
        );
    }

    @Override
    public void run(String[] args) {
        if(args.length > 0) {
            switch(args[0]) {
                case "help":
                default:
                    sendHelp();
                    break;
            }
        } else
            sendHelp();
    }

    /**
     * Send the help message for Quickplay Premium
     */
    public void sendHelp() {
        if(Quickplay.INSTANCE.premiumHelp != null) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(Quickplay.INSTANCE.premiumHelp, true, false));
        } else {
            final String premiumLink = "https://bugg.co/quickplay/premium";

            final IChatComponent chatComponent = new ChatComponentTranslation("quickplay.commands.quickplay.premium.helpMenuMissing", premiumLink).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
            final ChatStyle chatStyle = new ChatStyle().setColor(EnumChatFormatting.RED);
            chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentTranslation("quickplay.commands.quickplay.premium.helpMenuMissing.clickToOpen", premiumLink).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))));
            chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, premiumLink));
            chatComponent.setChatStyle(chatStyle);

            Quickplay.INSTANCE.messageBuffer.push(new Message(chatComponent, true, false));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final ArrayList<String> list = new ArrayList<>();
        list.add("help");
        return list;
    }
}
