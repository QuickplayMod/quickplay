package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.QuickplayKeybind;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.util.GsonPostProcessorFactory;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * ID: 15
 * Set the list keybinds to a new JSON object for this user.
 * This is currently only used to migrate keybinds from pre-2.1.0 to post-2.1.0.
 * @see co.bugg.quickplay.actions.serverbound.MigrateKeybindsAction
 *
 * Payload Order:
 * valid JSON that goes into keybinds.json
 */
public class SetKeybindsAction extends Action {

    public SetKeybindsAction() {}

    /**
     * Create a new SetKeybindsAction.
     * @param keybinds New keybinds to serialize and send to the client.
     */
    public SetKeybindsAction(List<QuickplayKeybind> keybinds) {
        super();
        this.id = 15;
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(keybinds).getBytes()));
    }

    @Override
    public void run() {
        final String json = this.getPayloadObjectAsString(0);
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonPostProcessorFactory()).create();
        Type listType = new TypeToken<ArrayList<QuickplayKeybind>>(){}.getType();
        if(Quickplay.INSTANCE.keybinds == null) {
            Quickplay.INSTANCE.keybinds = new ConfigKeybinds(false);
        }
        Quickplay.INSTANCE.keybinds.keybinds = gson.fromJson(json, listType);
        try {
            Quickplay.INSTANCE.keybinds.save();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.keybinds.migratingComplete")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
                    , false));
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.config.saveFailed", "keybinds")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }

    }
}
