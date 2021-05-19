package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.DateUtil;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumCommandAccount extends ACommand {

    public PremiumCommandAccount(ACommand parent) {
        super(
                parent,
                Collections.singletonList("account"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.account.help"),
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
        IChatComponent component = new QuickplayChatComponentTranslation("quickplay.premium.expiresIn",
                String.valueOf(DateUtil.calculateDaysUntil(Quickplay.INSTANCE.premiumExpirationDate)));
        // Append purchase page if it is available
        if(Quickplay.INSTANCE.purchasePageURL != null) {
            IChatComponent purchasePageLang = new QuickplayChatComponentTranslation("quickplay.premium.purchasePage");
            IChatComponent purchasePageLink = new ChatComponentText(Quickplay.INSTANCE.purchasePageURL);
            // Make clickable link
            purchasePageLink.setChatStyle(new ChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, Quickplay.INSTANCE.purchasePageURL)
            ).setColor(EnumChatFormatting.AQUA));
            component.appendText("\n\n");
            component.appendSibling(purchasePageLang);
            component.appendSibling(purchasePageLink);
        }
        component.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));

        Quickplay.INSTANCE.messageBuffer.push(new Message(component, true));
    }

    @Override
    public List<String> getTabCompletions(String[] strings) {
        return new ArrayList<>();
    }
}
