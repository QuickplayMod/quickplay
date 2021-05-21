package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import net.minecraft.command.ICommandSender;

/**
 * Improved Hypixel /hub command
 * /hub on Hypixel is poor, so this adds a few extra features to the command
 */
public class CommandMain extends BaseCommand {
    public static final String commandSyntax = "[lobbyNumber]";

    /**
     * Command the player sends to trigger this <code>ACommand</code>.
     */
    public final String command;
    /**
     * Command sent by the client behind the scenes used to go to different lobbies
     */
    public final String serverCommand;

    /**
     * Constructor
     * If only one argument is provided, that argument is used as both the {@link #command} and {@link #serverCommand}
     * @param command Command to use as {@link #command} and {@link #serverCommand}
     */
    public CommandMain(String command) {
        this(command, command);
    }

    /**
     * Constructor for having two different commands for the user & the client
     * @param command Command the user sends to trigger this <code>ACommand</code>
     * @param serverCommand Command this <code>ACommand</code> sends to the server when changing lobbies
     */
    public CommandMain(String command, String serverCommand) {
        super("<number>", command);
        if(command != null && command.length() > 0) {

            this.command = command;
        } else {
            throw new IllegalArgumentException("command cannot be null and must be at least one character in length");
        }

        if(serverCommand != null && serverCommand.length() > 0) {
            this.serverCommand = serverCommand;
        } else {
            throw new IllegalArgumentException("serverCommand cannot be null and must be at least one character in length");
        }
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(Quickplay.INSTANCE.checkEnabledStatus()) {
                // sendChatMessage is used here instead of chatBuffer.push, as chatBuffer.push would try
                // to execute as a client command which would loop infinitely
                if(Quickplay.INSTANCE.isOnHypixel()) {
                    if(args.length == 0) {
                        Quickplay.INSTANCE.minecraft.sendRemoteMessageDirect("/" + serverCommand);
                    } else {
                        // Two parameters or greater were sent
                        try {
                            // Lobby number we're going to go to
                            final int lobbyNumber = Integer.parseInt(args[0]);

                            // If not in main lobby, go to main lobby first
                            if(!Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentServer().startsWith("lobby")) {
                                Quickplay.INSTANCE.minecraft.sendRemoteMessageDirect("/" + serverCommand);

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }


                            // Swap lobbies after waiting a sec
                            Quickplay.INSTANCE.minecraft.sendRemoteMessage("/swaplobby " + lobbyNumber);
                        } catch(NumberFormatException e) {
                            // Send usage
                            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                                    new QuickplayChatComponentTranslation("quickplay.commands.hub.numberexception",
                                            "/" + command + " " + commandSyntax)
                                            .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
                        }
                    }

                    return;
                }
            }

            // Fallback
            final String argsString = String.join(" " , args);
            Quickplay.INSTANCE.minecraft.sendRemoteMessageDirect("/" + serverCommand + " " + argsString);
        });
    }
}
