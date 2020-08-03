package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.NoSubscriptionException;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PremiumCommandAuth extends ACommand {

    public PremiumCommandAuth(ACommand parent) {
        super(
                parent,
                Arrays.asList("auth", "login"),
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.premium.auth.help"),
                "",
                true,
                true,
                95,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                "quickplay.commands.quickplay.premium.auth.runningRequest").setChatStyle(new ChatStyle()
                .setColor(EnumChatFormatting.GREEN))));

        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                if(Quickplay.INSTANCE.verifyPremium()) {
                    Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                            "quickplay.commands.quickplay.premium.auth.done").setChatStyle(new ChatStyle()
                            .setColor(EnumChatFormatting.GREEN))));
                } else {
                    Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                            "quickplay.commands.quickplay.premium.auth.noSubscription").setChatStyle(new ChatStyle()
                            .setColor(EnumChatFormatting.RED))));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.commands.quickplay.premium.auth.error").setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.RED))));
            } catch (NoSubscriptionException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(new QuickplayChatComponentTranslation(
                        "quickplay.commands.quickplay.premium.auth.noSubscription").setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.RED))));
            }
        });
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
