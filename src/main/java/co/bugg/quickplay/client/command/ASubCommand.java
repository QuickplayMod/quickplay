package co.bugg.quickplay.client.command;

import java.util.List;

/**
 * Sub command abstract class
 */
public abstract class ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     * @param name Name of this sub command
     * @param helpMessage Help message for this sub command, usually displayed in a help menu
     * @param displayInHelpMenu Whether this sub command can be displayed in a help menu
     * @param displayInTabList Whether this sub command can be tabbed into chat
     * @param priority TODO the priority of this sub command in help menu and tab list
     */
    public ASubCommand(ASubCommandParent parent, String name, String helpMessage, boolean displayInHelpMenu, boolean displayInTabList, double priority) {
        this.parent = parent;
        this.name = name;
        this.helpMessage = helpMessage;
        this.displayInHelpMenu = displayInHelpMenu;
        this.displayInTabList = displayInTabList;
        this.priority = priority;
    }

    /**
     * The parent command of this sub command
     */
    private ASubCommandParent parent;
    /**
     * The name of this sub command (what's used when executing)
     */
    private String name;
    /**
     * Help message to give information about this sub command
     */
    private String helpMessage;
    /**
     * Whether this sub command can be displayed in a help menu
     */
    private boolean displayInHelpMenu;
    /**
     * Whether this sub command can be tabbed in chat
     */
    private boolean displayInTabList;
    /**
     * TODO the priority of this sub command in tab & help menu & such
     */
    private double priority;

    /**
     * Called when getting the possible tab completion options for this sub command
     * @param args Arguments provided when tabbing
     * @return A list of all tab completion options
     */
    public abstract List<String> getTabCompletions(String[] args);

    /**
     * Called when this sub command is executed
     * @param args Arguments provided when executing
     */
    public abstract void run(String[] args);

    /**
     * Getter for {@link #parent}
     * @return {@link #parent}
     */
    public ASubCommandParent getParent() {
        return parent;
    }

    /**
     * Getter for {@link #name}
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for {@link #helpMessage}
     * @return {@link #helpMessage}
     */
    public String getHelpMessage() {
        return helpMessage;
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
}
