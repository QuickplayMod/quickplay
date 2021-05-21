package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.wrappers.chat.ChatComponentTextWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to enable debug mode within the client - Will output action data to the log as it's received.
 */
public class SubCommandDebug extends ACommand {

    // TODO remove
    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandDebug(ACommand parent) {
        super(
                parent,
                Collections.singletonList("debug"),
                 "",
                "",
                false,
                false,
                85,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if(args.length > this.getDepth() && args[this.getDepth()].equals("printController")) {
            System.out.println(new Gson().toJson(Quickplay.INSTANCE.elementController));
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                    new ChatComponentTextWrapper("Printed element controller to console.").setStyle(
                            new ChatStyleWrapper().apply(Formatting.AQUA)
                    )));
        } else {
            Quickplay.INSTANCE.isInDebugMode = !Quickplay.INSTANCE.isInDebugMode;
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                    new ChatComponentTextWrapper(Quickplay.INSTANCE.isInDebugMode ? "DEBUG ON":"DEBUG OFF").setStyle(
                            new ChatStyleWrapper().apply(Quickplay.INSTANCE.isInDebugMode ? Formatting.GREEN : Formatting.RED)
                    )));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
