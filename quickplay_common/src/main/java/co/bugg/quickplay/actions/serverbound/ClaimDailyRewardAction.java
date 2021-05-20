package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.client.dailyreward.DailyRewardAppData;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

/**
 * ID: 49
 * Received by the server when the client is claiming their in-game daily reward.
 * Should only be accepted from Premium users.
 *
 * Payload Order:
 * Option index
 * Security token
 * Reward app data
 */
public class ClaimDailyRewardAction extends Action {

    public ClaimDailyRewardAction() {}

    /**
     * Create a new GetDailyRewardAction.
     * @param option The option the client has requested be claimed. This is an index.
     * @param securityToken Security token, received by {@link co.bugg.quickplay.actions.clientbound.SetDailyRewardDataAction}
     * @param appData App data object, received by {@link co.bugg.quickplay.actions.clientbound.SetDailyRewardDataAction}
     */
    public ClaimDailyRewardAction(int option, String securityToken, DailyRewardAppData appData) {
        super();
        this.id = 49;

        ByteBuffer optionBuf = ByteBuffer.allocate(4);
        optionBuf.putInt(option);
        optionBuf.rewind();
        this.addPayload(optionBuf);
        this.addPayload(ByteBuffer.wrap(securityToken.getBytes()));
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(appData).getBytes()));
    }
}
