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

public class GlyphCommandOffset extends GlyphCommand {

    public GlyphCommandOffset(ACommand parent) {
        super(
                parent,
                Collections.singletonList("offset"),
                I18n.format("quickplay.premium.command.glyph.offset.help"),
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
                        new ChatComponentTranslation("quickplay.premium.command.glyph.offset.illegal")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                return;
            }

            final double parsedArg = Double.parseDouble(args[3]);
            if(parsedArg > 2 || parsedArg < -3)
                throw new IllegalArgumentException("Passed argument out of bounds -3 and 2.");

            HashMap<String, String> params = new HashMap<>();
            params.put("yOffset", String.valueOf(parsedArg));

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
                    new ChatComponentTranslation("quickplay.premium.command.glyph.offset.illegal")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }
}
