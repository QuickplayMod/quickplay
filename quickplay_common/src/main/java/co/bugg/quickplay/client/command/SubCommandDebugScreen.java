package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.client.gui.QuickplayGuiScreen;
import co.bugg.quickplay.elements.Screen;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

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
                 Quickplay.INSTANCE.elementController.translate("DEBUG - REMOVE"),
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
        final String finalScr = scr;
        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
            Screen screenObj = Quickplay.INSTANCE.elementController.getScreen(finalScr);
            if(screenObj == null || !screenObj.passesPermissionChecks()) {
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                        new QuickplayChatComponentTranslation("quickplay.screenOpenFail")
                                .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
                        , false, false));
                return;
            }
            Quickplay.INSTANCE.minecraft.openGui(new QuickplayGuiScreen(Quickplay.INSTANCE.elementController.getScreen(finalScr)));
        });
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
