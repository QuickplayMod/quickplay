package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 18
 * Received by the server when the client has pressed a Quickplay button, or a keybind which points to a button.
 *
 * Payload Order:
 * Button key
 */
public class ButtonPressedAction extends Action {

    public ButtonPressedAction() {}

    /**
     * Create a new ButtonPressedAction.
     * @param buttonKey The key of the button which was pressed
     */
    public ButtonPressedAction(String buttonKey) {
        super();
        this.id = 18;
        this.addPayload(ByteBuffer.wrap(buttonKey.getBytes()));
    }
}
