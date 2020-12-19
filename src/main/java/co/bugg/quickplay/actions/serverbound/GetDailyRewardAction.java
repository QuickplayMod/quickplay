package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 47
 * Received by the server when the client is requesting their in-game daily reward data.
 * Should only be accepted from Premium users.
 *
 * Payload Order:
 * Code
 */
public class GetDailyRewardAction extends Action {

    public GetDailyRewardAction() {}

    /**
     * Create a new GetDailyRewardAction.
     * @param code The Daily Reward code found in the daily reward URL.
     */
    public GetDailyRewardAction(String code) {
        super();
        this.id = 47;

        this.addPayload(ByteBuffer.wrap(code.getBytes()));
    }
}
