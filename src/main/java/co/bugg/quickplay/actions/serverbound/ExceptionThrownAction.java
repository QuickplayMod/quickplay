package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.ReflectionUtil;
import co.bugg.quickplay.util.ServerChecker;
import com.google.gson.Gson;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * ID: 19
 * Received by the server when an exception is reported by the client.
 *
 * This Action should NOT be sent if the user has not agreed to send this data.
 * @see Quickplay#usageStats
 *
 * If you are a third party implementing Quickplay into your client, you may remove this Action from your client.
 * Any exceptions which are not sent by a user agent maintained by Quickplay developers will be disregarded. If you
 * wish for this information to be sent to you, create your own system or get in contact on the Quickplay Discord.
 *
 * If any of this information does not apply to your client, send an empty payload for that item.
 *
 * Payload Order:
 * The type of the exception
 * The message of the exception
 * The stacktrace of the exception
 * Minecraft version
 * Client version
 * Java version
 * OS name
 * Enabled state
 * Current IP
 */
public class ExceptionThrownAction extends Action {

    public ExceptionThrownAction() {}

    /**
     * Create a new ExceptionThrownAction.
     * @param throwable The throwable exception to report.
     */
    public ExceptionThrownAction(Throwable throwable) {
        super();
        this.id = 19;
        this.addPayload(ByteBuffer.wrap(throwable.getClass().toGenericString().getBytes()));
        this.addPayload(ByteBuffer.wrap(throwable.getMessage().getBytes()));
        this.addPayload(ByteBuffer.wrap(ExceptionUtils.getStackTrace(throwable).getBytes()));

        // Minecraft version
        try {
            this.addPayload(ByteBuffer.wrap(ReflectionUtil.getMCVersion().getBytes()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            this.addPayload(ByteBuffer.wrap("unknown".getBytes()));
        }
        // Client version, e.g. for Forge this is the Forge version, Hyperium would be Hyperium version, etc.
        try {
            this.addPayload(ByteBuffer.wrap(ReflectionUtil.getForgeVersion().getBytes()));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            this.addPayload(ByteBuffer.wrap("unknown".getBytes()));
        }
        // Java version
        this.addPayload(ByteBuffer.wrap(System.getProperty("java.version").getBytes()));
        // OS name
        this.addPayload(ByteBuffer.wrap(System.getProperty("os.name").getBytes()));

        // Enabled state
        ByteBuffer enabledBuf = ByteBuffer.allocate(1);
        enabledBuf.put((byte) (Quickplay.INSTANCE.isEnabled ? 1 : 0));
        enabledBuf.rewind();
        this.addPayload(enabledBuf);

        // Current IP
        this.addPayload(ByteBuffer.wrap(ServerChecker.getCurrentIP().getBytes()));
        // Mod list - Send empty item or empty JSON array if not applicable to client.
        final List<String> modList = new ArrayList<>();
        for(final ModContainer mod : Loader.instance().getModList()) {
            modList.add(mod.getName());
        }
        this.addPayload(ByteBuffer.wrap(new Gson().toJson(modList).getBytes()));
    }
}
