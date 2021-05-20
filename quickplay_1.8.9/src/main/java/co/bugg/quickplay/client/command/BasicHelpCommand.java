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

import java.util.*;
import java.util.stream.Collectors;

public class BasicHelpCommand extends ACommand {

    String prependedFullCommand = "";

    /**
     * Constructor
     *
     * @param parent            Parent command
     * @param helpMessage       Help message for this sub command, usually displayed in a help menu
     * @param displayInHelpMenu Whether this sub command can be displayed in a help menu
     * @param displayInTabList  Whether this sub command can be tabbed into chat
     * @param priority          the priority of this sub command in help menu and tab list (bigger = higher)
     * @param requiresPremium   Whether this command requires Premium to be used/displayed
     */
    public BasicHelpCommand(ACommand parent, String helpMessage, boolean displayInHelpMenu, boolean displayInTabList, double priority, boolean requiresPremium) {
        super(
                parent,
                Arrays.asList("help", "h", "?"),
                helpMessage,
                "<command>",
                displayInHelpMenu,
                displayInTabList,
                priority,
                requiresPremium,
                parent == null ? 0 : parent.getDepth() + 1
        );
        this.prependedFullCommand = getFullCommand();
    }

    private String getFullCommand() {
        StringBuilder str = new StringBuilder();
        ACommand parent = this.getParent();
        while(parent != null) {
            str.insert(0, parent.getName() + " ");
            parent = parent.getParent();
        }
        str.insert(0, "/");
        return str.toString();
    }


    @Override
    public void run(String[] args) {
        boolean separators;
        IChatComponent helpMessage = new ChatComponentText("");

        // If no argument is provided, send the basic help message for all commands.
        // Otherwise, send the help message and usage syntax for the provided command, if it exists.
        if(args.length <= getDepth()) {
            separators = true;
            // Duplicate
            List<ACommand> subCommands = new ArrayList<>(getParent().subCommands);
            // Sort by priority & remove items that can't be displayed
            subCommands = subCommands
                    .stream()
                    .filter(ACommand::canDisplayInHelpMenu)
                    .sorted(Comparator.comparing(ACommand::getPriority).reversed())
                    .collect(Collectors.toList());

            for(ListIterator<ACommand> iterator = subCommands.listIterator(); iterator.hasNext();) {
                ACommand subCommand = iterator.next();
                helpMessage.appendSibling(getFormattedHelpMessage(subCommand));
                if(iterator.hasNext()) {
                    helpMessage.appendText("\n");
                }
            }
        } else {
            separators = false;
            ACommand commandToDisplay = getParent().getCommand(args[this.getDepth()]);
            if(commandToDisplay != null) {
                helpMessage.appendSibling(new QuickplayChatComponentTranslation("quickplay.commands.usage"));
                helpMessage.appendText("\n");
                helpMessage.appendText(this.prependedFullCommand + commandToDisplay.getName() + " " + commandToDisplay.getUsage());
            } else {
                helpMessage.appendSibling(new QuickplayChatComponentTranslation("quickplay.commands.invalid"));
            }
            helpMessage.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
        }

        Quickplay.INSTANCE.messageBuffer.push(new Message(helpMessage, separators));
    }


    @Override
    public List<String> getTabCompletions(String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length < this.getDepth() + 2) {
            List<ACommand> subCommands = getParent().getSubCommands().stream()
                    // Get parent commands which allow being displayed via tab
                    .filter(ACommand::canDisplayInTabList)
                    // Filter out commands which don't start with what has already been typed
                    .filter(scmd -> scmd.getName().startsWith(args[args.length - 1]))
                    .sorted(Comparator.comparing(ACommand::getPriority).reversed()) // Sort
                    .collect(Collectors.toList());

            for(ACommand subCommand : subCommands) {
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
    public IChatComponent getFormattedHelpMessage(ACommand subCommand) {
        final String fullCommand = this.prependedFullCommand + subCommand.getName();

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
