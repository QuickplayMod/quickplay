package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Command for getting help with Quickplay Premium commands and displaying help text for each Premium command
 */
public class PremiumCommandHelp implements IPremiumCommand {

    public final SubCommandPremium parent;

    public PremiumCommandHelp(SubCommandPremium parent) {
        this.parent = parent;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getUsage() {
        return "[commandName]";
    }

    @Override
    public String getHelpText() {
        return "Displays Premium command help information";
    }

    @Override
    public void run(String[] args) {
        // First arg is always value of getName()
        // If no second argument specifying a specific command was provided
        if (args.length < 2) {
            sendBaseHelpMessage();
        } else {
            List<IPremiumCommand> filteredList = parent.premiumCommands.stream().filter(cmd -> cmd.getName().equals(args[1])).collect(Collectors.toList());
            if (filteredList.size() > 0) {
                final IChatComponent chatComponent = new ChatComponentText(filteredList.get(0).getHelpText() + "\n")
                        .appendSibling(new ChatComponentTranslation("quickplay.commands.usage").appendText("\n"))
                        .appendSibling(new ChatComponentText("/" + parent.getParent().getCommandName() + " " + parent.getName() + " " + filteredList.get(0).getName() + " " + filteredList.get(0).getUsage()))
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
                Quickplay.INSTANCE.messageBuffer.push(new Message(chatComponent, true));
            } else {
                sendBaseHelpMessage();
            }
        }
    }

    /**
     * Send the basic help message for premium commands
     */
    public void sendBaseHelpMessage() {
        final IChatComponent message = new ChatComponentText("");
        if (parent.premiumCommands.size() > 0) {
            for (ListIterator<IPremiumCommand> iter = parent.premiumCommands.listIterator(); iter.hasNext(); ) {
                final IPremiumCommand premiumCommand = iter.next();
                message.appendSibling(new ChatComponentText("/" + parent.getParent().getCommandName() + " " + parent.getName() + " " + premiumCommand.getName()))
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA));
                message.appendSibling(new ChatComponentText(" - ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
                message.appendSibling(new ChatComponentText(premiumCommand.getHelpText()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
                if (iter.hasNext()) message.appendText("\n");
            }
        } else {
            message.appendSibling(new ChatComponentTranslation("quickplay.commands.quickplay.premium.noCommands").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        }

        Quickplay.INSTANCE.messageBuffer.push(new Message(message, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final List<String> list = new ArrayList<>();
        if (args.length == 2) {
            // Add all commands that begin with what's already been typed out
            list.addAll(parent.premiumCommands.stream().filter(cmd -> cmd.getName().startsWith(args[1])).map(IPremiumCommand::getName).collect(Collectors.toList()));
        }

        return list;
    }
}
