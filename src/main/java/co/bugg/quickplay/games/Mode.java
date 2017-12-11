package co.bugg.quickplay.games;

import java.io.Serializable;

/**
 * Mode for a Hypixel game
 */
public class Mode implements Serializable {
    /**
     * Display name for this mode
     */
    public String name;
    /**
     * Command to play this mode
     */
    public String command;
    /**
     * Whether this mode's command is a normal /play command
     * or if it's a different command (different commands are
     * still specified in {@link #command}, just with
     * the actual command prepended)
     */
    public boolean customCommand;

    /**
     * Constructor
     * @param name Display name of this mode
     * @param command Command for this mode
     * @param customCommand Whether this mode has a custom command
     */
    public Mode(String name, String command, boolean customCommand) {
        this.name = name;
        this.command = command;
        this.customCommand = customCommand;
    }
}
