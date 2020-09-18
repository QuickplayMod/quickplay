package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * ID: 28
 * Finalize authentication by sending the client a session token and when this session expires. Also sends
 * information about this user's account and their Premium state.
 *
 * Payload Order:
 * Session token
 * Session expiration timestamp
 * Minecraft UUID
 * Discord ID, or empty string if non-existent
 * Google ID, or empty string if non-existent
 * isAdmin boolean
 * isPremium boolean
 * Premium expiration timestamp, or 0 if user has no premium.
 */
public class AuthCompleteAction extends Action {

    public AuthCompleteAction() {}

    /**
     * Create a new AuthCompleteAction.
     * @param sessionToken Session token to send.
     * @param sessionExpiration Datetime at which this session expires.
     * @param mcUuid Minecraft UUID associated with this user's account.
     * @param discordId Discord ID associated with this user's account.
     * @param googleId Google account ID associated with this user's account.
     * @param isAdmin Whether this user has Admin permissions.
     * @param isPremium Whether this user has a Premium subscription active.
     * @param premiumExpiration Datetime of when this user's Premium expires, or null if the user has no subscription.
     */
    public AuthCompleteAction(String sessionToken, Date sessionExpiration, String mcUuid, String discordId,
                              String googleId, boolean isAdmin, boolean isPremium, Date premiumExpiration) {
        super();
        this.id = 28;
        this.addPayload(ByteBuffer.wrap(sessionToken.getBytes()));

        ByteBuffer sessionExpirationBuf = ByteBuffer.allocate(4);
        sessionExpirationBuf.putInt((int) (sessionExpiration.getTime() / 1000));
        sessionExpirationBuf.rewind();
        this.addPayload(sessionExpirationBuf);

        this.addPayload(ByteBuffer.wrap(mcUuid.getBytes()));

        if(discordId != null) {
            this.addPayload(ByteBuffer.wrap(discordId.getBytes()));
        } else {
            this.addPayload(ByteBuffer.wrap(new byte[]{}));
        }
        if(googleId != null) {
            this.addPayload(ByteBuffer.wrap(googleId.getBytes()));
        } else {
            this.addPayload(ByteBuffer.wrap(new byte[]{}));
        }

        ByteBuffer isAdminBuf = ByteBuffer.allocate(1);
        isAdminBuf.put(isAdmin ? (byte) 1 : (byte) 0);
        isAdminBuf.rewind();
        this.addPayload(isAdminBuf);

        ByteBuffer isPremiumBuf = ByteBuffer.allocate(1);
        isPremiumBuf.put(isPremium ? (byte) 1 : (byte) 0);
        isAdminBuf.rewind();
        this.addPayload(isPremiumBuf);

        ByteBuffer premiumExpirationBuf = ByteBuffer.allocate(4);
        if(premiumExpiration != null) {
            premiumExpirationBuf.putInt((int) (premiumExpiration.getTime() / 1000));
        } else {
            premiumExpirationBuf.putInt(0);
        }
        premiumExpirationBuf.rewind();
        this.addPayload(premiumExpirationBuf);
    }

    @Override
    public void run() {
        Quickplay.INSTANCE.sessionKey = this.getPayloadObjectAsString(0);
        Date expires = new Date(this.getPayloadObject(1).getInt() * 1000);
        long sleepTime = expires.getTime() - new Date().getTime();
        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                Thread.sleep(sleepTime);
                Quickplay.INSTANCE.isAdminClient = false;
                Quickplay.INSTANCE.isPremiumClient = false;
                Quickplay.INSTANCE.premiumExpirationDate = new Date(0);
                Quickplay.INSTANCE.sessionKey = "";
                Quickplay.INSTANCE.socket.sendAction(new InitializeClientAction());
            } catch (ServerUnavailableException | InterruptedException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                Quickplay.INSTANCE.messageBuffer.push(new Message(
                        new ChatComponentTranslation("quickplay.failedToAuth")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)),
                        true
                ));
            }
        });
        Quickplay.INSTANCE.isAdminClient = this.getPayloadObject(5).get(0) != 0;
        Quickplay.INSTANCE.isPremiumClient = this.getPayloadObject(6).get(0) != 0;
        Quickplay.INSTANCE.premiumExpirationDate = new Date(this.getPayloadObject(7).getInt() * 1000);
        System.out.println("Authenticated with Quickplay backend.");
    }
}
