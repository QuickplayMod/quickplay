package co.bugg.quickplay.client.command.premium.glyph;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.render.PlayerGlyph;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.ResponseAction;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public abstract class GlyphCommand extends ACommand {

    public GlyphCommand(ACommand parent, List<String> aliases, String helpMessage, String usage,
                        boolean displayInHelpMenu, boolean displayInTabList, double priority, boolean requiresPremium, int depth) {
        super(parent, aliases, helpMessage, usage, displayInHelpMenu, displayInTabList, priority, requiresPremium, depth);
    }

    /**
     * Run a request as if it's a request to a Glyph endpoint and treating its response as such
     *
     * Client shouldn't send any messages to the user about their glyph request (with the exception of
     * IO errors & telling the user the request is started), as that'll all be handled by the web server.
     *
     * @param request Request to be sent & treated as a glyph request
     */
    public void runGlyphRequest(Request request) {
        Quickplay.INSTANCE.messageBuffer.push(new Message(
                new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.runningRequest")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))));

        Quickplay.INSTANCE.threadPool.submit(() -> {
            final WebResponse response = request.execute();
            if(response != null) {
                // Run any actions
                if(response.actions != null)
                    for(ResponseAction action : response.actions)
                        action.run();

                // Update own glyph if possible
                if(response.ok && response.content != null && response.content.getAsJsonObject().get("glyph") != null) {
                    try {
                        final PlayerGlyph newGlyph = new Gson().fromJson(response.content.getAsJsonObject().get("glyph"),
                                PlayerGlyph.class);
                        final List<PlayerGlyph> glyphs = Quickplay.INSTANCE.glyphs;

                        // Remove all glyph instances from this user & add the new glyph
                        final List<PlayerGlyph> glyphsToRemove = new ArrayList<>();
                        for (PlayerGlyph glyph : glyphs) {
                            // If this is this user's glyph
                            if (glyph.uuid.toString().equals(newGlyph.uuid.toString())) {
                                // Remove the glyph
                                glyphsToRemove.add(glyph);
                            }
                        }
                        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                            glyphs.removeAll(glyphsToRemove);
                            glyphs.add(newGlyph);
                        });

                    } catch(JsonSyntaxException e) {
                        e.printStackTrace();
                        Quickplay.INSTANCE.sendExceptionRequest(e);
                    }
                }

            } else {
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new QuickplayChatComponentTranslation("quickplay.commands.quickplay.premium.glyph.error")
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
                ));
            }
        });
    }
}
