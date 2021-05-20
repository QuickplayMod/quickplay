package co.bugg.quickplay.games;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * Gamemode on the Hypixel Network
 */
public class Game implements Serializable {
    /**
     * Display name of this game
     */
    public String name;
    /**
     * Name universal across languages, used for things like priority & favorites
     */
    public String unlocalizedName;
    /**
     * URL to the image file for this gamemode
     */
    public URL imageURL;
    /**
     * A list of all modes for this gamemode
     */
    public List<Mode> modes;

    /**
     * Constructor
     * @param name Display name of game
     * @param imageURL URL to image for game
     */
    public Game(String name, URL imageURL) {
        this.name = name;
        this.imageURL = imageURL;
    }
}
