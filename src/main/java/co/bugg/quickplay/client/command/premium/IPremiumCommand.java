package co.bugg.quickplay.client.command.premium;

import java.util.List;

/**
 * Premium sub command interface
 * <p>
 * Could probably be combined with {@link co.bugg.quickplay.client.command.ASubCommand}
 * at some point but isn't very necessary.
 *
 * @see SubCommandPremium
 */
public interface IPremiumCommand {

    /**
     * Get the name of this premium command
     * This is what users type in chat to trigger {@link #run(String[])}
     *
     * @return The name of this command
     */
    String getName();

    /**
     * Gets documentation on possible arguments for this command
     * Does not include the name, parent command name, etc.
     * e.g. [username &lt;page&gt;]
     *
     * @return A string containing arguments the user can possibly enter
     */
    String getUsage();

    /**
     * Gets the help text, describing what this command does.
     *
     * @return Help text
     */
    String getHelpText();

    /**
     * Called whenever the user executes this command.
     * A user can execute this command by running the parent premium command
     * with {@link #getName()} as the next argument
     *
     * @param args All arguments, minus the {@link co.bugg.quickplay.client.command.ASubCommand} if applicable (See it's documentation)
     */
    void run(final String[] args);

    /**
     * Called whenever the user pressed the tab key and this command is the last argument
     *
     * @param args All arguments, minus the {@link co.bugg.quickplay.client.command.ASubCommand} if applicable (See it's documentation)
     * @return A list of all options the user should be provided with for autocomplete
     */
    List<String> getTabCompletions(final String[] args);
}
