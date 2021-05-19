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

import java.util.Collections;

public class GlyphCommandOffset extends GlyphCommand {

    public GlyphCommandOffset(ACommand parent) {
        super(
                parent,
                Collections.singletonList("offset"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.glyph.offset.help"),
                "<number>",
                true,
                true,
                70,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        try {
            if(args.length < 4) {
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.offset.illegal")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                return;
            }

            final float parsedArg = Float.parseFloat(args[3]);
            if(parsedArg > 2 || parsedArg < -3) {
                throw new IllegalArgumentException("Passed argument out of bounds -3 and 2.");
            }

            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    final PlayerGlyph glyph = new PlayerGlyph(Minecraft.getMinecraft().getSession().getProfile().getId(),
                            null, null, parsedArg, null);
                    Quickplay.INSTANCE.socket.sendAction(new AlterGlyphAction(glyph));
                } catch (ServerUnavailableException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.messageBuffer.push(new Message(
                            new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                }
            });
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.offset.illegal")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }
}
