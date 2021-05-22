package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumCommandTransfer extends ACommand {

    public PremiumCommandTransfer(ACommand parent) {
        super(
                parent,
                Collections.singletonList("transfer"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.transfer.help"),
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
        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.transfer.todo")
                        .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
        ));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}