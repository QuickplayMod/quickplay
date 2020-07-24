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

public class GlyphCommandReset extends GlyphCommand {

    public GlyphCommandReset(ACommand parent) {
        super(
                parent,
                Collections.singletonList("reset"),
                I18n.format("quickplay.premium.command.glyph.reset.help"),
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
        HashMap<String, String> params = new HashMap<>();
        params.put("reset", String.valueOf(true));

        final Request request = Quickplay.INSTANCE.requestFactory.newGlyphModificationRequest(params);

        if(request != null) {
            runGlyphRequest(request);
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new ChatComponentTranslation("quickplay.premium.command.glyph.error")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }

    }
}
