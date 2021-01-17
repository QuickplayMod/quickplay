package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.serverbound.AlterGlyphAction;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.render.PlayerGlyph;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class GlyphCommandReset extends GlyphCommand {

    public GlyphCommandReset(ACommand parent) {
        super(
                parent,
                Collections.singletonList("reset"),
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.premium.glyph.reset.help"),
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
                // To reset, create a new glyph instance with an empty URL. All other values are default.
                final PlayerGlyph glyph = new PlayerGlyph(Minecraft.getMinecraft().getSession().getProfile().getId(),
                        new URL(""));
                Quickplay.INSTANCE.socket.sendAction(new AlterGlyphAction(glyph));
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.error")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }
        });
    }
}
