package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.serverbound.AlterGlyphAction;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.render.PlayerGlyph;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;

import java.util.Collections;

public class GlyphCommandHeight extends GlyphCommand {

    public GlyphCommandHeight(ACommand parent) {
        super(
                parent,
                Collections.singletonList("height"),
                Quickplay.INSTANCE.elementController.translate("quickplay.commands.quickplay.premium.glyph.height.help"),
                "<number>",
                true,
                true,
                60,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        try {
            if(args.length < 4) {
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.height.illegal")
                                .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
                return;
            }

            final int parsedArg = Integer.parseInt(args[3]);
            if(parsedArg > 40 || parsedArg < 1) {
                throw new IllegalArgumentException("Passed argument out of bounds 1 and 40.");
            }

            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    final PlayerGlyph glyph = new PlayerGlyph(Quickplay.INSTANCE.minecraft.getUuid(),
                            null, parsedArg, null, null);
                    Quickplay.INSTANCE.socket.sendAction(new AlterGlyphAction(glyph));
                } catch (ServerUnavailableException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                            new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
                }
            });

        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                    new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.height.illegal")
                            .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
        }
    }
}
