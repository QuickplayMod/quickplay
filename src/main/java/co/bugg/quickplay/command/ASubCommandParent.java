package co.bugg.quickplay.command;

import co.bugg.quickplay.Quickplay;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ASubCommandParent implements ICommand{
    final List<String> aliases = new ArrayList<>();
    final List<ASubCommand> subCommands = new ArrayList<>();
    ASubCommand defaultCommand;

    public ASubCommandParent(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }


    /**
     * Add the provided sub command to list of sub commands
     * @param subCommand Sub command to add
     * @param defaultCommand Whether this is the default command
     */
    public void addSubCommand(ASubCommand subCommand, boolean defaultCommand) {
        subCommands.add(subCommand);
        if(defaultCommand) this.defaultCommand = subCommand;
    }

    /**
     * @see this#addSubCommand(ASubCommand, boolean)
     */
    public void addSubCommand(ASubCommand subCommand) {
        addSubCommand(subCommand, false);
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " " + defaultCommand.getName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(args.length == 0) {
                defaultCommand.run(new String[]{});
            } else {
                ASubCommand subCommand = getCommand(args[0]);
                if(subCommand == null) {
                    defaultCommand.run(new String[]{});
                } else {
                    subCommand.run(removeFirstArgument(args));
                }
            }
        });
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> tabCompletionOptions = new ArrayList<>();

        if(args.length == 0) {
            return getDefaultTabCompletions();
        } else {
            ASubCommand subCommand = getCommand(args[0]);
            if(subCommand != null) {
                return subCommand.getTabCompletions();
            } else {
                getDefaultTabCompletions();
            }
        }

        return tabCompletionOptions;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    /**
     * Remove the first argument, for passing to
     * subcommands.
     * @param args Args to remove first arg from
     * @return Modified array
     */
    public String[] removeFirstArgument(String[] args) {
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        return argsList.toArray(new String[argsList.size()]);
    }

    /**
     * Filters sub commands into a list of
     * all sub commands' names that are set
     * to be able to be displayed in the
     * tab completion list
     * @return List of command names
     */
    public List<String> getDefaultTabCompletions() {
        return subCommands.stream()
                .filter(ASubCommand::canDisplayInTabList)
                .map(ASubCommand::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get the subcommand with the provided name from
     * the list of subcommands in this object
     * @param name Name of command to get
     * @return The sub command, or null if nonexistant
     */
    public ASubCommand getCommand(String name) {
        return subCommands.stream()
                .filter(subCommand -> subCommand.getName().equals(name))
                .findFirst().orElse(null);
    }

    /**
     * Get all sub commands of this command
     * @return A list of sub commands
     */
    public List<ASubCommand> getSubCommands() {
        return subCommands;
    }
}
