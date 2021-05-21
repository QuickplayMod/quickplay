package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.*;

import java.util.*;
import java.util.stream.Collectors;

public class BasicHelpCommand extends ACommand {

    String prependedFullCommand;

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
        IChatComponentWrapper helpMessage = new ChatComponentTextWrapper("");

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
                helpMessage.appendSibling(this.getFormattedHelpMessage(subCommand));
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
            helpMessage.setStyle(new ChatStyleWrapper().apply(Formatting.RED));
        }

        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(helpMessage, separators));
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
    public IChatComponentWrapper getFormattedHelpMessage(ACommand subCommand) {
        final String fullCommand = this.prependedFullCommand + subCommand.getName();

        final IChatComponentWrapper msg = new ChatComponentTextWrapper("");

        final IChatComponentWrapper command = new ChatComponentTextWrapper(fullCommand);
        command.setStyle(new ChatStyleWrapper().apply(Formatting.AQUA));

        final IChatComponentWrapper separator = new ChatComponentTextWrapper(" - ");
        separator.setStyle(new ChatStyleWrapper().apply(Formatting.GRAY));

        final IChatComponentWrapper helpMessage = new ChatComponentTextWrapper(subCommand.getHelpMessage());
        helpMessage.setStyle(new ChatStyleWrapper().apply(Formatting.YELLOW));

        msg.appendSibling(command);
        msg.appendSibling(separator);
        msg.appendSibling(helpMessage);

        final ChatStyleWrapper msgStyle = new ChatStyleWrapper();
        msgStyle.setClickEvent(new ClickEventWrapper(ClickEventWrapper.Action.SUGGEST_COMMAND, fullCommand));

        final IChatComponentWrapper hoverText = new ChatComponentTextWrapper("Click to put " + fullCommand + " in chat box");
        final ChatStyleWrapper hoverStyle = new ChatStyleWrapper().apply(Formatting.GRAY).apply(Formatting.ITALIC);
        hoverText.setStyle(hoverStyle);

        msgStyle.setHoverEvent(new HoverEventWrapper(HoverEventWrapper.Action.SHOW_TEXT, hoverText));
        msg.setStyle(msgStyle);

        return msg;
    }
}
