package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * ID: 50
 * Push an edit event into the client's edit history. This should only be sent to admins.
 */
public class PushEditHistoryEventAction extends Action {

    public PushEditHistoryEventAction() {}

    /**
     * Create a new PushEditHistoryEventAction.
     * @param timestamp Date timestamp of when this edit took place.
     * @param editedBy Account ID of the account which executed this edit.
     * @param itemType Type of item this edit was executing on.
     * Either "screen", "button", "aliased_action" or "translation".
     * @param itemKey The key of the item which was edited in this event.
     * @param deleted Whether or not this item was deleted in this event.
     * @param prevVersion Object containing the previous version of this item, before this edit.
     */
    public PushEditHistoryEventAction(Date timestamp, int editedBy, String itemType, String itemKey, boolean deleted,
                                      JsonObject prevVersion) {
        super();
        this.id = 50;
        ByteBuffer timestampBuf = ByteBuffer.allocate(4);
        timestampBuf.putInt((int) (timestamp.getTime() / 1000));
        timestampBuf.rewind();
        this.addPayload(timestampBuf);
        ByteBuffer editedByBuf = ByteBuffer.allocate(4);
        editedByBuf.putInt(editedBy);
        editedByBuf.rewind();
        this.addPayload(editedByBuf);
        this.addPayload(ByteBuffer.wrap(itemType.getBytes()));
        this.addPayload(ByteBuffer.wrap(itemKey.getBytes()));
        ByteBuffer deletedBuf = ByteBuffer.allocate(1);
        deletedBuf.put((byte) (deleted ? 1 : 0));
        deletedBuf.rewind();
        this.addPayload(deletedBuf);
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(prevVersion).getBytes()));
    }

    @Override
    public void run() {
        // Currently only used in the web panel.
    }
}
