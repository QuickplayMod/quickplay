package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.client.gui.game.QuickplayGuiScreen;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to open up a debug screen
 */
public class SubCommandDebugScreen extends ACommand {

    // TODO remove
    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDebugScreen(ACommand parent) {
        super(
                parent,
                Collections.singletonList("ds"),
                 Quickplay.INSTANCE.translator.get("DEBUG - REMOVE"),
                "",
                false,
                true,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        String scr = "MAIN";
        if(args.length > 1) {
            scr = args[1];
        }
        String finalScr = scr;
        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiScreen(Quickplay.INSTANCE.screenMap.get(finalScr)));
            // TODO check restrictions on Screen
        });
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
