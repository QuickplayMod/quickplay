package co.bugg.quickplay.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.nio.CharBuffer;
import java.util.List;
import java.util.ListIterator;

public class SubCommandHelp extends ASubCommand {

    public SubCommandHelp(ASubCommandParent parent) {
        super(
                parent,
                "help",
                new ChatComponentTranslation("quickplay.commands.quickplay.help.help").getUnformattedText(),
                true,
                true,
                100.0
        );
    }

    @Override
    public void run(String[] args) {
        IChatComponent helpMessage = new ChatComponentText("");

        for(ListIterator<ASubCommand> iterator = getParent().subCommands.listIterator(); iterator.hasNext();) {
            ASubCommand subCommand = iterator.next();
            if(subCommand.canDisplayInHelpMenu()) {
                helpMessage.appendSibling(subCommand.getFormattedHelpMessage());
                if(iterator.hasNext()) helpMessage.appendText("\n");
            }
        }

        Quickplay.INSTANCE.messageBuffer.push(new Message(helpMessage, true));
    }

    @Override
    public List<String> getTabCompletions() {
        return null;
    }
}
