package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.actions.Action;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.InvocationTargetException;
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
        final String className = this.getPayloadObjectAsString(0);
        String[] args = new String[this.payloadCount() - 1];
        for(int i = 0; i < this.payloadCount() - 1; i++) {
            args[i] = this.getPayloadObjectAsString(i);
        }

        final Class clazz;
        try {
            clazz = Class.forName(className);

        if (clazz == null || (clazz != GuiScreen.class && !GuiScreen.class.isAssignableFrom(clazz))) {
                throw new IllegalArgumentException("class corresponding to className could not be found or is not of type GuiScreen");
            }

            Class[] paramsClasses = new Class[args.length];
            int i = 0;
            for (Object param : args) {
                paramsClasses[i++] = param.getClass();
            }

            final GuiScreen screen = (GuiScreen) clazz.getDeclaredConstructor(paramsClasses)
                    .newInstance(args);
            QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                Minecraft.getMinecraft().displayGuiScreen(screen);
            });
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException |
                IllegalAccessException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }
}
