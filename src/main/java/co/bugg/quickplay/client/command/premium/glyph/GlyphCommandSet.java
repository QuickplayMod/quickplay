package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

public class GlyphCommandSet extends GlyphCommand {

    public GlyphCommandSet(ACommand parent) {
        super(
                parent,
                Collections.singletonList("set"),
                I18n.format("quickplay.premium.command.glyph.set.help"),
                "<imageURL>",
                true,
                true,
                90,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        try {
            if(args.length < 4) {
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new ChatComponentTranslation("quickplay.premium.command.glyph.set.illegal")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
                return;
            }

            final URL url = new URL(args[3]);

            HashMap<String, String> params = new HashMap<>();
            params.put("url", String.valueOf(url));

            final Request request = Quickplay.INSTANCE.requestFactory.newGlyphModificationRequest(params);

            if(request != null)
                runGlyphRequest(request);
            else
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new ChatComponentTranslation("quickplay.premium.command.glyph.error")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        } catch(MalformedURLException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new ChatComponentTranslation("quickplay.premium.command.glyph.set.illegal")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }
}
