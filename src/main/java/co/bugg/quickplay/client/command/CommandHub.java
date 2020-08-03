package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

/**
 * Improved Hypixel /hub command
 * /hub on Hypixel is poor, so this adds a few extra features to the command
 */
public class CommandHub extends BaseCommand {
    public static final String commandSyntax = "[lobbyName | lobbyNumber] [lobbyNumber]";

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
    public CommandHub(String command) {
        this(command, command);
    }

    /**
     * Constructor for having two different commands for the user & the client
     * @param command Command the user sends to trigger this <code>ACommand</code>
     * @param serverCommand Command this <code>ACommand</code> sends to the server when changing lobbies
     */
    public CommandHub(String command, String serverCommand) {
        super(commandSyntax, command);
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
                if(Quickplay.INSTANCE.onHypixel) {
                    if(args.length == 0) {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + serverCommand);
                    } else if(args.length == 1) {
                        // Check if the user is trying to swap lobbies by checking
                        // if they sent a lobby number instead of a lobby name
                        try {
                            final int lobbyNumber = Integer.parseInt(args[0]);
                            Quickplay.INSTANCE.chatBuffer.push("/swaplobby " + lobbyNumber);
                        } catch(NumberFormatException e) {
                            // It's a string so just send them to that lobby instead of lobby number
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + serverCommand + " " + args[0]);
                        }
                    } else {
                        // Two parameters or greater were sent
                        try {
                            // Lobby number we're going to go to
                            final int lobbyNumber = Integer.parseInt(args[1]);
                            // First go to the lobby itself
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + serverCommand + " " + args[0]);

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // Swap lobbies after waiting a sec
                            Quickplay.INSTANCE.chatBuffer.push("/swaplobby " + lobbyNumber);
                        } catch(NumberFormatException e) {
                            // Send usage
                            Quickplay.INSTANCE.messageBuffer.push(new Message(
                                    new QuickplayChatComponentTranslation("quickplay.commands.hub.numberexception",
                                            "/" + command + " " + commandSyntax)
                                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                        }
                    }

                    return;
                }
            }

            // Fallback
            final String argsString = String.join(" " , args);
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + serverCommand + " " + argsString);
        });
    }
}
