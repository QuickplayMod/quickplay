package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;

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
     * @param isAdmin {boolean} Whether this user has Admin permissions.
     * @param isPremium {boolean} Whether this user has a Premium subscription active.
     * @param premiumExpiration {Date} Datetime of when this user's Premium expires, or null if the user has no subscription.
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
        // TODO load other payload items
        System.out.println("Authenticated with Quickplay backend.");
    }
}
