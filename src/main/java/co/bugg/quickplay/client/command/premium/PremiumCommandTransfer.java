package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumCommandTransfer extends ACommand {

    public PremiumCommandTransfer(ACommand parent) {
        super(
                parent,
                Collections.singletonList("transfer"),
                I18n.format("quickplay.commands.quickplay.premium.transfer.help"),
                "",
                true,
                true,
                80,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public String getUsage() {
        return "<UUID>";
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(
                new ChatComponentTranslation("quickplay.commands.quickplay.premium.transfer.todo")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
        ));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
