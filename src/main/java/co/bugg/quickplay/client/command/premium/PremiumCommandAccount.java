package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.DateUtil;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumCommandAccount extends ACommand {

    public PremiumCommandAccount(ACommand parent) {
        super(
                parent,
                Collections.singletonList("account"),
                I18n.format("quickplay.commands.quickplay.premium.account.help"),
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
        IChatComponent component = new ChatComponentTranslation("quickplay.premium.expiresIn",
                DateUtil.calculateDaysUntil(Quickplay.INSTANCE.expirationTime));
        // Append purchase page if it is available
        if(Quickplay.INSTANCE.purchasePageURL != null) {
            IChatComponent purchagePageLang = new ChatComponentTranslation("quickplay.premium.purchasePage");
            IChatComponent purchasePageLink = new ChatComponentText(Quickplay.INSTANCE.purchasePageURL);
            // Make clickable link
            purchasePageLink.setChatStyle(new ChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, Quickplay.INSTANCE.purchasePageURL)
            ).setColor(EnumChatFormatting.AQUA));
            component.appendText("\n\n");
            component.appendSibling(purchagePageLang);
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
