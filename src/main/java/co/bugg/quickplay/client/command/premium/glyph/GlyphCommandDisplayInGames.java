package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GlyphCommandDisplayInGames extends GlyphCommand {
    public GlyphCommandDisplayInGames(ACommand parent) {
        super(
                parent,
                Arrays.asList("ingame", "displayingame"),
                Quickplay.INSTANCE.translator.get("quickplay.commands.quickplay.premium.glyph.ingame.help"),
                "<true|false>",
                true,
                true,
                40,
                true,
                parent == null ? 0 : parent.getDepth() + 1
        );
    }

    @Override
    public void run(String[] args) {
        if(args.length < 4) {
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.ingame.illegal")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            return;
        }
        final boolean parsedArg = Boolean.parseBoolean(args[3]);

        HashMap<String, String> params = new HashMap<>();
        params.put("ingame", String.valueOf(parsedArg));

        final Request request = Quickplay.INSTANCE.requestFactory.newGlyphModificationRequest(params);

        if(request != null)
            runGlyphRequest(request);
        else
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.error")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));

    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final ArrayList<String> list = new ArrayList<>();
        if(args.length <= 4) {
            list.add("true");
            list.add("false");
        }

        return list;
    }
}
