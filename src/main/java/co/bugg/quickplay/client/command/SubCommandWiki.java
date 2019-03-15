package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to open up the Quickplay wiki
 */
public class SubCommandWiki extends ASubCommand {

    /**
     * Constructor
     *
     * @param parent Parent command
     */
    public SubCommandWiki(ACommand parent) {
        super(
                parent,
                "wiki",
                I18n.format("quickplay.commands.quickplay.wiki.help"),
                "",
                true,
                true,
                86
        );
    }

    @Override
    public void run(String[] args) {
        try {
            Desktop.getDesktop().browse(new URI("https://bugg.co/quickplay/wiki/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
