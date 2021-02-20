package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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
        ITextComponent helpMessage = new TextComponentString("");

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
                helpMessage.appendSibling(new TextComponentTranslation("quickplay.commands.usage"));
                helpMessage.appendText("\n");
                helpMessage.appendText("/" + commandToDisplay.getParent().getName() + " " +
                        commandToDisplay.getName() + " " + commandToDisplay.getUsage());
                helpMessage.setStyle(new Style().setColor(TextFormatting.RED));
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
    public ITextComponent getFormattedHelpMessage(ASubCommand subCommand) {
        final String fullCommand = "/" + subCommand.getParent().getName() + " " + subCommand.getName();

        final ITextComponent msg = new TextComponentString("");

        final ITextComponent command = new TextComponentString(fullCommand);
        command.setStyle(new Style().setColor(TextFormatting.AQUA));

        final ITextComponent separator = new TextComponentString(" - ");
        separator.setStyle(new Style().setColor(TextFormatting.GRAY));

        final ITextComponent helpMessage = new TextComponentString(subCommand.getHelpMessage());
        helpMessage.setStyle(new Style().setColor(TextFormatting.YELLOW));

        msg.appendSibling(command);
        msg.appendSibling(separator);
        msg.appendSibling(helpMessage);

        final Style msgStyle = new Style();
        msgStyle.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand));

        final ITextComponent hoverText = new TextComponentString("Click to put " + fullCommand + " in chat box");
        final Style hoverStyle = new Style().setColor(TextFormatting.GRAY).setItalic(true);
        hoverText.setStyle(hoverStyle);

        msgStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        msg.setStyle(msgStyle);

        return msg;
    }
}
