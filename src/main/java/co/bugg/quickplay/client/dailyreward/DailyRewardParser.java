package co.bugg.quickplay.client.dailyreward;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.ResponseAction;
import co.bugg.quickplay.http.response.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.HashMap;

/**
 * Class for handling the user's daily reward from in-game
 */
public class DailyRewardParser {

    /**
     * Since event cancelling doesn't seem to work in {@link this#guiOpened(GuiOpenEvent)}, I've opted to
     * simply save the GUI in this field and change the GUI to be displayed from a book to the loading GUI
     */
    DailyRewardGuiLoading overrideBookGui;

    @SubscribeEvent
    public void guiOpened(GuiOpenEvent event) {
        if(event.gui instanceof GuiScreenBook)
            event.gui = this.overrideBookGui;
        if(event.gui == null) // If all GUIs are closed then stop listening
            Quickplay.INSTANCE.unregisterEventHandler(this);
    }
    /**
     * Constructor
     *
     * @param code Daily reward code
     */
    public DailyRewardParser(String code) throws IOException {

        overrideBookGui = new DailyRewardGuiLoading();
        Quickplay.INSTANCE.registerEventHandler(this);
        // Open GUI screen next tick; Fixes cursor not being released bug
        // https://www.minecraftforge.net/forum/topic/36866-189mouse-not-showing-up-in-gui/
        QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
            Minecraft.getMinecraft().displayGuiScreen(overrideBookGui);
        });

        final HashMap<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());
        params.put("key", Quickplay.INSTANCE.sessionKey);
        final Request request = Quickplay.INSTANCE.requestFactory.newRequest("https://bugg.co/quickplay/mod/premium/reward", params);

        final WebResponse response = request.execute();

        if(response == null) {
            throw new IOException("Failed to get a valid response from the web server! Null response");
        }

        for(ResponseAction action : response.actions) {
            action.run();
        }

        if(!response.ok || response.content == null) {
            if(response.content != null)
                System.out.println(response.content.getAsJsonObject().get("error").getAsString());
            throw new IOException("Failed to get a valid response from the web server! No content or not OK");
        }

        Gson gson = new Gson();
        final DailyRewardAppData appdata = gson.fromJson(response.content.getAsJsonObject().get("appData"), DailyRewardAppData.class);
        final String securityToken = response.content.getAsJsonObject().get("securityToken").getAsString();
        final JsonObject i18n = response.content.getAsJsonObject().get("i18n").getAsJsonObject();

        Quickplay.INSTANCE.unregisterEventHandler(this);
        Minecraft.getMinecraft().displayGuiScreen(new DailyRewardGui(securityToken, appdata, i18n, null));
    }
}
