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
     * Constructor
     * @param name Display name of this mode
     * @param command Command for this mode
     */
    public Mode(String name, String command) {
        this.name = name;
        this.command = command;
    }
}
