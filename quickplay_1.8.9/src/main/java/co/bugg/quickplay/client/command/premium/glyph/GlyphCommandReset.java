package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.serverbound.DeleteGlyphAction;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;

public class GlyphCommandReset extends GlyphCommand {

    public GlyphCommandReset(ACommand parent) {
        super(
                parent,
                Collections.singletonList("reset"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.glyph.reset.help"),
                "",
                true,
                true,
                50,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                Quickplay.INSTANCE.socket.sendAction(new DeleteGlyphAction(
                        Minecraft.getMinecraft().getSession().getProfile().getId()));
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }
        });
    }
}
