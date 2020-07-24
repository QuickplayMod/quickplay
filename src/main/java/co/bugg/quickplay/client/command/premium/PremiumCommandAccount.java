package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.DateUtil;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumCommandAccount extends ACommand {

    public PremiumCommandAccount(ACommand parent) {
        super(
                parent,
                Collections.singletonList("account"),
                I18n.format("quickplay.premium.command.account.help"),
                "",
                true,
                true,
                91,
                false,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] strings) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(
                new ChatComponentTranslation("quickplay.premium.expiresIn",
                        DateUtil.calculateDaysUntil(Quickplay.INSTANCE.expirationTime))
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)), true));
    }

    @Override
    public List<String> getTabCompletions(String[] strings) {
        return new ArrayList<>();
    }
}
