package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

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
                              String googleId, boolean isAdmin, boolean isPremium, Date premiumExpiration,
                              String hypixelRank, String hypixelPackageRank, boolean isHypixelBuildTeam,
                              boolean isHypixelBuildTeamAdmin) {
        super();
        this.id = 28;

        if(sessionToken == null || sessionExpiration == null) {
            return;
        }

        this.addPayloadString(sessionToken, "");
        this.addPayloadInteger((int) (sessionExpiration.getTime() / 1000));
        this.addPayloadString(mcUuid, "");
        this.addPayloadString(discordId, "");
        this.addPayloadString(googleId, "");
        this.addPayloadBoolean(isAdmin);
        this.addPayloadBoolean(isPremium);
        if(premiumExpiration == null) {
            this.addPayloadInteger(0);
        } else {
            this.addPayloadInteger((int) (premiumExpiration.getTime() / 1000));
        }
        this.addPayloadString(hypixelRank, "NONE");
        this.addPayloadString(hypixelPackageRank, "NONE");
        this.addPayloadBoolean(isHypixelBuildTeam);
        this.addPayloadBoolean(isHypixelBuildTeamAdmin);
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
        Quickplay.INSTANCE.premiumExpirationDate = new Date(this.getPayloadObject(7).getInt() * 1000L);
        Quickplay.INSTANCE.hypixelRank = this.getPayloadObjectAsString(8);
        Quickplay.INSTANCE.hypixelPackageRank = this.getPayloadObjectAsString(9);
        Quickplay.INSTANCE.isHypixelBuildTeamMember = this.getPayloadObject(10).get(0) != 0;
        Quickplay.INSTANCE.isHypixelBuildTeamAdmin = this.getPayloadObject(11).get(0) != 0;
        System.out.println("Authenticated with Quickplay backend.");
    }
}
