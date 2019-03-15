package co.bugg.quickplay.games;

/**
 * Extension off of basic modes that has a namespace for use in the Quickplay Party system.
 */
public class PartyMode extends Mode {
    /**
     * Namespace for this mode
     * <p>
     * Follows the scheme: <code>unlocalizedGameName/command</code>
     * Command and unlocalized game name should both have slashes stripped.
     * e.x.: <code>skywars/play solo_insane</code>
     */
    public String namespace;

    /**
     * Constructor
     *
     * @param name      Name of the mode to display
     * @param command   Command of the mode
     * @param namespace Unique namespace of this mode
     */
    public PartyMode(String name, String command, String namespace) {
        super(name, command);
        this.namespace = namespace;
    }

    // TODO getNamespace() method instead of field, that way people can't spoof it by editing files
}
