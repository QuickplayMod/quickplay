package co.bugg.quickplay.games;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class Game implements Serializable {
    public String name;
    public String lobbyName;
    public URL imageURL;
    public List<Mode> modes;
}
