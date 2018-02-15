package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class CommandHub extends ACommand {
    public static final String commandSyntax = "[lobbyName | lobbyNumber] [lobbyNumber]";

    public final String command;
    public CommandHub(String command) {
        super(command);
        if(command != null && command.length() > 0)
            this.command = command;
        else throw new IllegalArgumentException("command cannot be null and must be at least one character in length");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(Quickplay.INSTANCE.checkEnabledStatus()) {
                // sendChatMessage is used here instead of chatBuffer.push, as chatBuffer.push would try
                // to execute as a client command which would loop infinitely
                if(Quickplay.INSTANCE.onHypixel) {
                    if(args.length == 0) {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + command);
                    } else if(args.length == 1) {
                        // Check if the user is trying to swap lobbies by checking
                        // if they sent a lobby number instead of a lobby name
                        try {
                            final int lobbyNumber = Integer.parseInt(args[0]);
                            Quickplay.INSTANCE.chatBuffer.push("/swaplobby " + lobbyNumber);
                        } catch(NumberFormatException e) {
                            // It's a string so just send them to that lobby instead of lobby number
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + command + " " + args[0]);
                        }
                    } else {
                        try {
                            final int lobbyNumber = Integer.parseInt(args[1]);
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + command + " " + args[0]);

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Quickplay.INSTANCE.chatBuffer.push("/swaplobby " + lobbyNumber);
                        } catch(NumberFormatException e) {
                            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.hub.numberexception", "/" + command + " " + commandSyntax).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                        }
                    }

                    return;
                }
            }

            final String argsString = String.join(" " , args);
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + command + " " + argsString);
        });
    }
}
