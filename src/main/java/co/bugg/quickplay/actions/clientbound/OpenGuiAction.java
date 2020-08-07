package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

import java.nio.ByteBuffer;

/**
 * ID: 10
 * Open a class with the specified classpath as a GUI on the client. Should extend GuiScreen.
 *
 * Payload Order:
 * class path
 * Each argument is an individual item
 */
public class OpenGuiAction extends Action {

    public OpenGuiAction () {}

    /**
     * Create a new OpenGuiAction.
     * @param classpath The path of the class to open as a GUI.
     * @param args The arguments to pass to the class constructor.
     */
    public OpenGuiAction (String classpath, String... args) {
        super();
        this.id = 10;
        this.addPayload(ByteBuffer.wrap(classpath.getBytes()));

        if(args.length <= 0) {
            return;
        }
        for (final String arg : args) {
            this.addPayload(ByteBuffer.wrap(arg.getBytes()));
        }
    }

    @Override
    public void run() {
        // TODO
    }
}
