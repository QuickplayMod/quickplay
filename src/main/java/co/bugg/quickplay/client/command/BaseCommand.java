package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.InvalidCommandException;
import co.bugg.quickplay.util.Message;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand extends ACommand implements ICommand {

    /**
     * Constructor
     * @param aliases All aliases for the command
     */
    public BaseCommand(String usage, String... aliases) {
        super(null, Arrays.asList(aliases), "", usage,
                true, true, 0.0, false, 0);
    }

    @Override
    public List<String> getCommandAliases() {
        return this.getAliases();
    }

    @Override
    public String getCommandName() {
        return this.getName();
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return this.getUsage();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        this.sendAnalyticalData(args);
        try {
            this.run(args);
        } catch (InvalidCommandException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new ChatComponentTranslation("quickplay.commands.invalid")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return this.getTabCompletions(args);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return this.getCommandName().compareTo(o.getCommandName());
    }


    /**
     * Send analytical data about this command's execution to Google Analytics.
     * @param args The arguments sent with the command.
     */
    public void sendAnalyticalData(String[] args) {
        // Send analytical data to Google
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.ga.createEvent("commands", "Execute Command")
                            .setEventLabel("/" + getName() + " " + String.join(" ", args))
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
