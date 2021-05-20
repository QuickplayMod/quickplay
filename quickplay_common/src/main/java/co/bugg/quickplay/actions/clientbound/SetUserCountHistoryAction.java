package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * ID: 37
 *
 * Set the array of user counts to be displayed in the user count history graph. Included is the timestamp of the
 * first item in the graph, the timestamp of the last item in the graph, and an array of each item in the graph,
 * in order. It is assumed that each item in the graph is evenly spaced (e.g. index x is y minutes after index x-1 and
 * y minutes before index x+1)
 * This should be a protected Action, only sent to admins.
 *
 * Payload Order:
 * start
 * end
 * array of counts
 */
public class SetUserCountHistoryAction extends Action {

    public SetUserCountHistoryAction() {}

    /**
     * Create a new SetUserCountHistoryAction.
     * @param start Datetime for the first item in the array.
     * @param end Datetime for the last item in the array.
     * @param arr Array of items, assumed to be evenly spaced, with the earliest at the beginning
     * and most recent at the end.
     */
    public SetUserCountHistoryAction(Date start, Date end, int[] arr) {
        super();
        this.id = 37;

        ByteBuffer startBuf = ByteBuffer.allocate(4);
        startBuf.putInt((int) (start.getTime() / 1000));
        startBuf.rewind();
        this.addPayload(startBuf);
        ByteBuffer endBuf = ByteBuffer.allocate(4);
        endBuf.putInt((int) (end.getTime() / 1000));
        endBuf.rewind();
        this.addPayload(endBuf);
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(arr).getBytes()));
    }

    @Override
    public void run() {
        // Currently only used in the web panel.
    }
}
