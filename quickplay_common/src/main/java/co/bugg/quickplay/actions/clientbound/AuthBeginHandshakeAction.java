package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.AuthMojangEndHandshakeAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ID: 26
 * Begin authentication with the backend by sending a handshake token to the client.
 *
 * Payload Order:
 * Handshake token
 */
public class AuthBeginHandshakeAction extends Action {

    public AuthBeginHandshakeAction() {}

    /**
     * Create a new AuthBeginHandshakeAction.
     * @param handshakeToken Handshake token to send
     */
    public AuthBeginHandshakeAction(String handshakeToken) {
        super();
        this.id = 26;
        this.addPayload(ByteBuffer.wrap(handshakeToken.getBytes()));
    }


    /**
     * Send HTTP request to the Minecraft session server, indicating the client has joined the server
     * @param handshake Handshake secret to be included in the server hash
     * @throws IOException when status code is not equal to 204
     */
    public void sendSessionServerRequest(final String handshake) throws IOException {
        try {
            final String serverHash = AuthBeginHandshakeAction.hexDigest(handshake + getCompliantUuid());

            final HttpPost post = new HttpPost("https://sessionserver.mojang.com/session/minecraft/join");
            final JsonObject body = createBody(serverHash);
            final StringEntity entity = new StringEntity(body.toString(), StandardCharsets.UTF_8);

            post.setEntity(entity);
            post.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = HttpClients.createDefault().execute(post);

            if(response.getStatusLine().getStatusCode() != 204) {
                throw new IOException("Invalid response of " + response.getStatusLine().getStatusCode() + " from " +
                        "sessions server. Expected: 204");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a JSON body for the HTTP request
     * @param serverId serverHash to include. {@see "https://wiki.vg/Protocol_Encryption#Authentication"}
     * @return JsonObject body. Convert to json with <code>.toString()</code>.
     */
    public JsonObject createBody(final String serverId) {
        JsonObject base = new JsonObject();
        base.addProperty("accessToken", Minecraft.getMinecraft().getSession().getToken());
        base.addProperty("selectedProfile", getCompliantUuid());
        base.addProperty("serverId", serverId);
        return base;
    }

    /**
     * Get UUID compliant with the protocol by removing dashes
     * @return The client's UUID but without dashes
     */
    private static String getCompliantUuid() {
        return Minecraft.getMinecraft().getSession().getProfile().getId()
                .toString().replace("-", "");
    }

    /**
     * Digest w/ Minecraft's proprietary hash
     * @see "https://gist.github.com/unascribed/70e830d471d6a3272e3f"
     * @param str String to hash
     * @return Hashed result
     */
    private static String hexDigest(String str) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
            byte[] digest = md.digest(strBytes);
            return new BigInteger(digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                this.sendSessionServerRequest(this.getPayloadObjectAsString(0));
                Quickplay.INSTANCE.socket.sendAction(new AuthMojangEndHandshakeAction(Minecraft.getMinecraft().getSession().getUsername()));
            } catch (IOException | ServerUnavailableException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(
                        new QuickplayChatComponentTranslation("quickplay.failedToAuth")
                        .setStyle(new ChatStyleWrapper().apply(Formatting.RED))
                ));
            }
        });
    }
}
