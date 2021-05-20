package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.dailyreward.DailyRewardAppData;
import co.bugg.quickplay.client.dailyreward.DailyRewardGui;
import co.bugg.quickplay.client.dailyreward.DailyRewardGuiLoading;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;

import java.nio.ByteBuffer;

/**
 * ID: 48
 * Store the user's active daily reward data and, if necessary, open up the Daily Reward GUI for in-game claiming.
 * This should only be sent to Premium users, most likely.
 *
 * Payload Order:
 * Reward app data
 * Security token
 * Translation information
 * Google Analytics token
 */
public class SetDailyRewardDataAction extends Action {

    public SetDailyRewardDataAction() {}

    /**
     * Create a new SetDailyRewardDataAction.
     * @param appData daily reward app data object. Contains information about the user's
     * claiming options.
     * @param securityToken state-managing security token used by Hypixel.
     * @param i18n Translation data
     * @param analyticsToken Google Analytics token. The client can send this to Hypixel's Google Analytics
     * so they have proper analytical data.
     */
    public SetDailyRewardDataAction(DailyRewardAppData appData, String securityToken, JsonObject i18n, String analyticsToken) {
        super();
        this.id = 48;
        final Gson gson = new Gson();
        this.addPayload(ByteBuffer.wrap(gson.toJson(appData).getBytes()));
        this.addPayload(ByteBuffer.wrap(securityToken.getBytes()));
        this.addPayload(ByteBuffer.wrap(gson.toJson(i18n).getBytes()));
        this.addPayload(ByteBuffer.wrap(analyticsToken.getBytes()));
    }

    @Override
    public void run() {
        // Screen should only be opened if the loading screen is open, or if the book screen is open (in case it
        // failed to catch)
        if(!(Minecraft.getMinecraft().currentScreen instanceof DailyRewardGuiLoading) &&
            !(Minecraft.getMinecraft().currentScreen instanceof GuiScreenBook)) {
            return;
        }
        final Gson gson = new Gson();
        final DailyRewardAppData appData = gson.fromJson(this.getPayloadObjectAsString(0), DailyRewardAppData.class);
        final String securityToken = this.getPayloadObjectAsString(1);
        final JsonObject i18n = gson.fromJson(this.getPayloadObjectAsString(2), JsonObject.class);
        final String analyticsToken = this.getPayloadObjectAsString(3);
        Minecraft.getMinecraft().displayGuiScreen(new DailyRewardGui(securityToken, appData, i18n, analyticsToken));
    }
}
