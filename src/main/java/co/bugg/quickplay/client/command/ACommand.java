package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parent command for all sub commands
 */
public abstract class ACommand implements ICommand {
    /**
     * All possible aliases for the command (including the main
     * command, which is in index 0)
     */
    final List<String> aliases = new ArrayList<>();
    /**
     * All sub commands under this command
     * Default command, in case of invalid command provided
     * or no command provided, is in index 0.
     */
    final List<ASubCommand> subCommands = new ArrayList<>();

    /**
     * Constructor
     * @param aliases All aliases for the command
     */
    public ACommand(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    /**
     * Add the provided sub command to list of sub commands
     * @param subCommand Sub command to add
     */
    public void addSubCommand(ASubCommand subCommand) {
        subCommands.add(subCommand);
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
        return "/" + getCommandName() + " " + subCommands.get(0).getName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Send analytical data to Google
        if(Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.ga.createEvent("commands", "Execute Command")
                            .setEventLabel("/" + getCommandName() + " " + String.join(" ", args))
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Only run if there are actually sub commands available; Otherwise it's pointless
        if(subCommands.size() > 0) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                if(args.length == 0) {
                    subCommands.get(0).run(new String[]{});
                } else {
                    ASubCommand subCommand = getCommand(args[0]);
                    if(subCommand == null) {
                        subCommands.get(0).run(new String[]{});
                    } else {
                        subCommand.run(removeFirstArgument(args));
                    }
                }
            });
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> tabCompletionOptions = new ArrayList<>();

        if(args.length < 2) {
            tabCompletionOptions.addAll(getDefaultTabCompletions(args[args.length - 1]));
        } else {
            ASubCommand subCommand = getCommand(args[0]);
            if(subCommand != null) {
                tabCompletionOptions.addAll(subCommand.getTabCompletions(removeFirstArgument(args)));
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
     * @param limiter only returns sub commands that have names that start with this
     * @return List of command names
     */
    public List<String> getDefaultTabCompletions(String limiter) {
        return subCommands.stream()
                .filter(ASubCommand::canDisplayInTabList)
                .filter(scmd -> scmd.getName().startsWith(limiter))
                .sorted(Comparator.comparing(ASubCommand::getPriority))
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
