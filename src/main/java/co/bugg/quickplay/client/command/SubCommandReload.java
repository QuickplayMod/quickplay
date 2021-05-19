package co.bugg.quickplay.client.command;

import co.bugg.quickplay.ElementController;
import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import co.bugg.quickplay.actions.serverbound.SetClientSettingsAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sub command to reload the connection with the backend by sending a new InitializeClientAction.
 */
public class SubCommandReload extends ACommand {

    // TODO remove
    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandReload(ACommand parent) {
        super(
                parent,
                Collections.singletonList("reload"),
                 Quickplay.INSTANCE.elementController.translate("Reload the Quickplay backend connection and game list."),
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
        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.reload.reloading")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW))
                ));
                // Attempt to connect to server if not already connected.
                if(!Quickplay.INSTANCE.socket.isOpen()) {
                    Quickplay.INSTANCE.socket.connectBlocking();
                } else { // If already connected, instead re-initialize the client.
                    Quickplay.INSTANCE.socket.sendAction(new InitializeClientAction());
                    Quickplay.INSTANCE.socket.sendAction(new SetClientSettingsAction(Quickplay.INSTANCE.settings));
                }

                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.reload.success")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
                ));
            } catch (ServerUnavailableException | InterruptedException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
                ));
                // Failed to connect to Quickplay backend -- Load gamelist from cache
                try {
                    Quickplay.INSTANCE.elementController = ElementController.loadCache();
                } catch (FileNotFoundException fileNotFoundException) {
                    System.out.println("Failed to load cached elements.");
                    fileNotFoundException.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
