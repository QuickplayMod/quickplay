package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.HashMap;

public class GlyphCommandHeight extends GlyphCommand {

    public GlyphCommandHeight(ACommand parent) {
        super(
                parent,
                Collections.singletonList("height"),
                I18n.format("quickplay.premium.command.glyph.height.help"),
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
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new ChatComponentTranslation("quickplay.premium.command.glyph.height.illegal")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                return;
            }

            final int parsedArg = Integer.parseInt(args[3]);
            if(parsedArg > 40 || parsedArg < 1)
                throw new IllegalArgumentException("Passed argument out of bounds 1 and 40.");

            HashMap<String, String> params = new HashMap<>();
            params.put("height", String.valueOf(parsedArg));

            final Request request = Quickplay.INSTANCE.requestFactory.newGlyphModificationRequest(params);

            if(request != null)
                runGlyphRequest(request);
            else
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new ChatComponentTranslation("quickplay.premium.command.glyph.error")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new ChatComponentTranslation("quickplay.premium.command.glyph.height.illegal")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }
}
