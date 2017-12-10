package co.bugg.quickplay.games;

import java.io.Serializable;

public class Mode implements Serializable {
    public String name;
    public String command;
    public boolean customCommand;

    public Mode(String name, String command, boolean customCommand) {
        this.name = name;
        this.command = command;
        this.customCommand = customCommand;
    }
}
