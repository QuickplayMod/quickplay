package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.text.*;

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
        if(args.length < 2) {
            sendBaseHelpMessage();
        } else {
            List<IPremiumCommand> filteredList = parent.premiumCommands.stream().filter(cmd -> cmd.getName().equals(args[1])).collect(Collectors.toList());
            if(filteredList.size() > 0) {
                final ITextComponent chatComponent = new TextComponentString(filteredList.get(0).getHelpText() + "\n")
                        .appendSibling(new TextComponentTranslation("quickplay.commands.usage").appendText("\n"))
                        .appendSibling(new TextComponentString("/" + parent.getParent().getName() + " " + parent.getName() + " " + filteredList.get(0).getName() + " " + filteredList.get(0).getUsage()))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW));
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
        final ITextComponent message = new TextComponentString("");
        if(parent.premiumCommands.size() > 0) {
            for(ListIterator<IPremiumCommand> iter = parent.premiumCommands.listIterator(); iter.hasNext();) {
                final IPremiumCommand premiumCommand = iter.next();
                message.appendSibling(new TextComponentString("/" + parent.getParent().getName() + " " + parent.getName() + " " + premiumCommand.getName()))
                    .setStyle(new Style().setColor(TextFormatting.AQUA));
                message.appendSibling(new TextComponentString(" - ").setStyle(new Style().setColor(TextFormatting.GRAY)));
                message.appendSibling(new TextComponentString(premiumCommand.getHelpText()).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                if(iter.hasNext()) message.appendText("\n");
            }
        } else {
            message.appendSibling(new TextComponentTranslation("quickplay.commands.quickplay.premium.noCommands").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        Quickplay.INSTANCE.messageBuffer.push(new Message(message, true));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final List<String> list = new ArrayList<>();
        if(args.length == 2)  {
            // Add all commands that begin with what's already been typed out
            list.addAll(parent.premiumCommands.stream().filter(cmd -> cmd.getName().startsWith(args[1])).map(IPremiumCommand::getName).collect(Collectors.toList()));
        }

        return list;
    }
}
