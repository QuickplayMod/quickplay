package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PremiumCommandAuth extends ACommand {

    public PremiumCommandAuth(ACommand parent) {
        super(
                parent,
                Arrays.asList("auth", "login"),
                Quickplay.INSTANCE.elementController.translate(""),
                "",
                false,
                true,
                95,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation(
                "quickplay.commands.quickplay.premium.auth.deprecated", "/qp reload")
                .setStyle(new ChatStyleWrapper().apply(Formatting.YELLOW))));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
