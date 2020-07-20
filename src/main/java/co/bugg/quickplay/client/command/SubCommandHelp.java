package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Sub command for the "help" message
 */
public class SubCommandHelp extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandHelp(ACommand parent) {
        super(
                parent,
                "help",
                I18n.format("quickplay.commands.quickplay.help.help"),
                "[subcommand]",
                true,
                true,
                100.0
        );
    }

    @Override
    public void run(String[] args) {
        boolean separators;
        IChatComponent helpMessage = new ChatComponentText("");

        // If an argument is provided, tell the user how to use that command, if it exists.
        if(args.length == 0) {
            separators = true;
            // Duplicate
            List<ASubCommand> subCommands = new ArrayList<>(getParent().subCommands);
            // Sort by priority & remove items that can't be displayed
            subCommands = subCommands
                    .stream()
                    .filter(ASubCommand::canDisplayInHelpMenu)
                    .sorted(Comparator.comparing(ASubCommand::getPriority).reversed())
                    .collect(Collectors.toList());

            for(ListIterator<ASubCommand> iterator = subCommands.listIterator(); iterator.hasNext();) {
                ASubCommand subCommand = iterator.next();
                helpMessage.appendSibling(getFormattedHelpMessage(subCommand));
                if(iterator.hasNext()) {
                    helpMessage.appendText("\n");
                }
            }
        } else {
            separators = false;
            ASubCommand commandToDisplay = getParent().getCommand(args[0]);
            if(commandToDisplay != null) {
                helpMessage.appendSibling(new ChatComponentTranslation("quickplay.commands.usage"));
                helpMessage.appendText("\n");
                helpMessage.appendText("/" + commandToDisplay.getParent().getCommandName() + " " +
                        commandToDisplay.getName() + " " + commandToDisplay.getUsage());
                helpMessage.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
            }
        }

        Quickplay.INSTANCE.messageBuffer.push(new Message(helpMessage, separators));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        List<String> list = new ArrayList<>();

        List<ASubCommand> subCommands = getParent().getSubCommands().stream()
                // Get parent commands which allow being displayed via tab
                .filter(ASubCommand::canDisplayInTabList)
                // Filter out commands which don't start with what has already been typed
                .filter(scmd -> scmd.getName().startsWith(args[args.length - 1]))
                .sorted(Comparator.comparing(ASubCommand::getPriority).reversed()) // Sort
                .collect(Collectors.toList());

        if(args.length < 2) {
            for(ASubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
        }

        return list;
    }

    /**
     * Format the help message for the specified sub command
     * @param subCommand Sub command to format for
     * @return A chat-ready formatted help message
     */
    public IChatComponent getFormattedHelpMessage(ASubCommand subCommand) {
        final String fullCommand = "/" + subCommand.getParent().getCommandName() + " " + subCommand.getName();

        final IChatComponent msg = new ChatComponentText("");

        final IChatComponent command = new ChatComponentText(fullCommand);
        command.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA));

        final IChatComponent separator = new ChatComponentText(" - ");
        separator.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));

        final IChatComponent helpMessage = new ChatComponentText(subCommand.getHelpMessage());
        helpMessage.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));

        msg.appendSibling(command);
        msg.appendSibling(separator);
        msg.appendSibling(helpMessage);

        final ChatStyle msgStyle = new ChatStyle();
        msgStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand));

        final IChatComponent hoverText = new ChatComponentText("Click to put " + fullCommand + " in chat box");
        final ChatStyle hoverStyle = new ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
        hoverText.setChatStyle(hoverStyle);

        msgStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        msg.setChatStyle(msgStyle);

        return msg;
    }
}
