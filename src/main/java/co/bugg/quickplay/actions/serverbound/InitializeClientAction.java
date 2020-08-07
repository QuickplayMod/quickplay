package co.bugg.quickplay.actions.serverbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.ReflectionUtil;
import net.minecraft.client.Minecraft;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * ID: 25
 * Received by the server when the client first initializes the socket. Intended to send client metadata.
 *
 * As an attempt to make porting Quickplay to other clients as easy as possible, only the first three payload items
 * are required. If you are a third party implementing Quickplay into your client, the other items may not be
 * relevant to your client, or your implementation or legal obligations may vary, rendering them difficult or
 * impossible to include. Additionally, they are not relevant to the Quickplay backend, but instead are used
 * to target a better user experience, debug issues, and gather user analytics.
 *
 * On the other hand, the player UUID, user agent, Quickplay version, and Minecraft language ARE required. The
 * Quickplay backend uses these items to determine what actions to send to the user, and what those actions should
 * contain in their payload. If these items are not included, the socket connection will be disconnected. If they are
 * not accurate, the user could receive actions which they should not receive, which could result in incorrect
 * buttons/translations or, at worst, client crashes.
 *
 * If a player UUID is not relevant to your client (e.g. your client only supports offline mode), submit a UUID of
 * all 0's. If you are not sure what your Quickplay user agent should be, it does not matter as long as you are
 * confident that it is unique to your client. If you are not sure what your Quickplay version should be, use the
 * version of Quickplay from which you are porting. If a Minecraft language is not relevant to your client, use
 * the default language of your client or "en_US".
 *
 * Payload Order:
 * Player UUID
 * User agent - This is the name of the client which the user is using.
 * Quickplay version
 * Minecraft language
 * Minecraft version
 * Client version - This is the version of the user agent, e.g. for Forge, it'd be the Forge version.
 */
public class InitializeClientAction extends Action {

    /**
     * Create a new InitializeClientAction.
     */
    public InitializeClientAction() {
        super();
        this.id = 25;
        // MC UUID
        this.addPayload(ByteBuffer.wrap(Minecraft.getMinecraft().getSession().getProfile().getId().toString().getBytes()));
        // User agent - Should be updated for implementations into other clients, e.g. Badlion, Hyperium, etc.
        this.addPayload(ByteBuffer.wrap("Quickplay Forge".getBytes()));
        // Quickplay version
        this.addPayload(ByteBuffer.wrap(Reference.VERSION.getBytes()));
        // Minecraft language
        this.addPayload(ByteBuffer.wrap(Minecraft.getMinecraft().gameSettings.language.getBytes()));

        // Optional data depending on the user's privacy settings.
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.sendUsageStats) {
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
        }

    }
}
