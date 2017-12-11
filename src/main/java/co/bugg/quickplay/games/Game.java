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
     * Name of the Hypixel lobby for this game
     * {@code null} for no lobby available
     */
    public String lobbyName;
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
     * @param lobbyName Lobby name for game
     * @param imageURL URL to image for game
     */
    public Game(String name, String lobbyName, URL imageURL) {
        this.name = name;
        this.lobbyName = lobbyName;
        this.imageURL = imageURL;
    }
}
