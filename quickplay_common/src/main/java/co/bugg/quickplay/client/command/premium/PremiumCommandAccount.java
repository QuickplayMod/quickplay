package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.DateUtil;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.chat.*;

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
        IChatComponentWrapper component = new QuickplayChatComponentTranslation("quickplay.premium.expiresIn",
                String.valueOf(DateUtil.calculateDaysUntil(Quickplay.INSTANCE.premiumExpirationDate)));
        // Append purchase page if it is available
        if(Quickplay.INSTANCE.purchasePageURL != null) {
            IChatComponentWrapper purchasePageLang = new QuickplayChatComponentTranslation("quickplay.premium.purchasePage");
            IChatComponentWrapper purchasePageLink = new ChatComponentTextWrapper(Quickplay.INSTANCE.purchasePageURL);
            // Make clickable link
            purchasePageLink.setStyle(new ChatStyleWrapper().setClickEvent(
                    new ClickEventWrapper(ClickEventWrapper.Action.OPEN_URL, Quickplay.INSTANCE.purchasePageURL)
            ).apply(Formatting.AQUA));
            component.appendText("\n\n");
            component.appendSibling(purchasePageLang);
            component.appendSibling(purchasePageLink);
        }
        component.setStyle(new ChatStyleWrapper().apply(Formatting.YELLOW));

        Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(component, true));
    }

    @Override
    public List<String> getTabCompletions(String[] strings) {
        return new ArrayList<>();
    }
}
