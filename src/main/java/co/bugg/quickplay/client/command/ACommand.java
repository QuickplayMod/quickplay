package co.bugg.quickplay.client.command;

import co.bugg.quickplay.util.InvalidCommandException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent command for all sub commands
 */
public abstract class ACommand {

    /**
     * Constructor
     * @param parent Parent command
     * @param aliases List of aliases for this sub command - First one is the base name.
     *                Must be non-null and contain at least one item.
     * @param helpMessage Help message for this sub command, usually displayed in a help menu
     * @param usage Command syntax. See {@link #usage}
     * @param displayInHelpMenu Whether this sub command can be displayed in a help menu
     * @param displayInTabList Whether this sub command can be tabbed into chat
     * @param priority the priority of this sub command in help menu and tab list (bigger = higher)
     * @param requiresPremium Whether this command requires Premium to be used/displayed
     * @param depth Depth of this command in the subcommand chain. Base commands are 0, Next level is 1, etc.
     */
    public ACommand(ACommand parent, List<String> aliases, String helpMessage, String usage, boolean displayInHelpMenu,
                    boolean displayInTabList, double priority, boolean requiresPremium, int depth) {
        if(aliases == null || aliases.size() <= 0) {
            throw new IllegalArgumentException("Aliases must be non-null and contain at least one item.");
        }
        this.parent = parent;
        this.aliases = aliases;
        this.helpMessage = helpMessage;
        this.usage = usage;
        this.displayInHelpMenu = displayInHelpMenu;
        this.displayInTabList = displayInTabList;
        this.priority = priority;
        this.requiresPremium = requiresPremium;
        this.depth = depth;
    }

    /**
     * All sub commands under this command
     * Default command, in case of invalid command provided
     * or no command provided, is in index 0.
     */
    protected final List<ACommand> subCommands = new ArrayList<>();
    /**
     * The parent command of this sub command
     */
    private final ACommand parent;
    /**
     * The aliases of this sub command (what's used when executing). First one is the base name.
     */
    private final List<String> aliases;
    /**
     * Help message to give information about this sub command
     */
    private final String helpMessage;
    /**
     * Syntax or example usage of parameters for this command
     * Example: "<game> <lobby> [yes|no]"
     */
    private final String usage;
    /**
     * Whether this sub command can be displayed in a help menu
     */
    private final boolean displayInHelpMenu;
    /**
     * Whether this sub command can be tabbed in chat
     */
    private final boolean displayInTabList;
    /**
     * the priority of this sub command in tab & help menu & such
     */
    private final double priority;
    /**
     * State of whether this command requires Premium. If this is true, non-premium users should not see it.
     */
    private final boolean requiresPremium;
    /**
     * Depth of this command in the subcommand chain. Base commands are 0, Next level is 1, etc.
     */
    private final int depth;

    /**
     * Called when getting the possible tab completion options for this sub command
     * @param args Arguments provided when tabbing
     * @return A list of all tab completion options
     */
    public List<String> getTabCompletions(String[] args) {
        List<String> tabCompletionOptions = new ArrayList<>();

        if(args.length < this.depth + 2) {
            for(final ACommand subCommand : this.getSubCommands()) {
                for(int i = 0; i < subCommand.getAliases().size(); i++) {
                    final String alias = subCommand.getAliases().get(i);
                    // If the user hasn't began typing an argument, only provide the first command instead of all aliases.
                    if(args[args.length - 1].length() <= 0 && i != 0) {
                        continue;
                    }
                    // Otherwise, check if the typed input is the beginning to any alias, not just the first one.
                    if(!alias.startsWith(args[args.length - 1])) {
                        continue;
                    }
                    tabCompletionOptions.add(alias);
                }
            }
        } else {
            ACommand subCommand = getCommand(args[this.depth]);
            if(subCommand != null) {
                tabCompletionOptions.addAll(subCommand.getTabCompletions(args));
            }
        }

        return tabCompletionOptions;
    }

    /**
     * Add the provided sub command to list of sub commands
     * @param subCommand Sub command to add
     */
    public void addSubCommand(ACommand subCommand) {
        subCommands.add(subCommand);
    }

    /**
     * Called when this sub command is executed
     * First argument (this command's name) is removed from param <code>args</code>.
     * @param args Arguments provided when executing. The first argument is the first term surrounded by spaces after
     *             the actual command name. E.g., in <code>/quickplay a b c</code>, <code>args[0].equals("a")</code>
     *             is true.
     */
    public void run(String[] args) throws InvalidCommandException {
        if(this.subCommands.size() <= 0) {
            throw new InvalidCommandException();
        }
        // If args.length < this.depth, then the previous level should've ran. If args.length == this.depth,
        // this level's custom implementation should run. If args.length > this.depth, the next level deep should run,
        // if it exists.
        if(args.length > this.depth) {
            ACommand subCommand = getCommand(args[this.depth]);
            if(subCommand == null) {
                throw new InvalidCommandException();
            }
            subCommand.run(args);
        }
    }

    /**
     * Getter for {@link #parent}
     * @return {@link #parent}
     */
    public ACommand getParent() {
        return parent;
    }

    /**
     * Getter for the first item in {@link #aliases}
     * @return the first item in {@link #aliases}
     */
    public String getName() {
        return aliases.get(0);
    }

    /**
     * Getter for {@link #aliases}
     * @return {@link #aliases}
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Getter for {@link #helpMessage}
     * @return {@link #helpMessage}
     */
    public String getHelpMessage() {
        return helpMessage;
    }

    /**
     * Getter for {@link #usage}
     * @return {@link #usage}
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Getter for {@link #displayInHelpMenu}
     * @return {@link #displayInHelpMenu}
     */
    public boolean canDisplayInHelpMenu() {
        return displayInHelpMenu;
    }

    /**
     * Getter for {@link #displayInTabList}
     * @return {@link #displayInTabList}
     */
    public boolean canDisplayInTabList() {
        return displayInTabList;
    }

    /**
     * Getter for {@link #priority}
     * @return {@link #priority}
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Getter for {@link #depth}
     * @return {@link #depth}
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Getter for {@link #requiresPremium}
     * @return {@link #requiresPremium}
     */
    public boolean isPaywalled() { return requiresPremium; }

    /**
     * Get all sub commands of this command
     * @return A list of sub commands
     */
    public List<ACommand> getSubCommands() {
        return subCommands;
    }

    /**
     * Get the subcommand with the provided name from
     * the list of subcommands in this object
     * @param name Name of command to get
     * @return The sub command, or null if nonexistant
     */
    public ACommand getCommand(String name) {
        if(name == null) {
            return null;
        }
        for(final ACommand subCommand : this.getSubCommands()) {
            for(final String alias : subCommand.getAliases()) {
                if(name.equals(alias)) {
                    return subCommand;
                }
            }
        }
        return null;
    }

}
