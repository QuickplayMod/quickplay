package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.ResponseAction;
import co.bugg.quickplay.http.response.WebResponse;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Premium authentication flow:
 *     1. Client requests mod/premium/startAuth with UUID.
 *     2. Server responds with unique secret and associates that secret with the users UUID.
 *     3. Client uses that secret as a salt in hexDigest hashing, along with the users UUID.
 *     4. Client sends the generated hash to https://sessionserver.mojang.com/session/minecraft/join
 *     5. Client requests mod/premium/auth/end with UUID.
 *     6. Server repeats steps 3 and 4, except to https://sessionserver.mojang.com/session/minecraft/hasJoined
 *     7. Server deletes the unique secret, as it is no longer valid.
 *     8. Server generates a unique session token, associates it with the user, and sends it to the client.
 *     9. Client uses this unique session token in future requests for protected resources.
 *          Tokens will expire after time, at which point the client can re-auth for a new one
 *
 * For more info, {@see "https://wiki.vg/Protocol_Encryption#Authentication"}
 */
public class PremiumAuthenticator {

    final SecureRandom csprng = new SecureRandom();

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public String getHandshakeSecret() throws IOException, NoSubscriptionException {
        final Request begin = Quickplay.INSTANCE.requestFactory.newPremiumStartAuthRequest();
        final WebResponse response = begin.execute();

        if(response == null || !response.ok) {
            String error = "NO ERROR RECEIVED";
            if(response != null && response.content != null && response.content.getAsJsonObject().get("error") != null) {
                error = response.content.getAsJsonObject().get("error").getAsString();

                if(error.equals("no_subscription"))
                    throw new NoSubscriptionException();
            }

            throw new IOException("Response from web server for startAuth is null or not-ok. Error: " + error);
        }

        for(final ResponseAction action : response.actions) {
            action.run();
        }

        if(response.content == null || response.content.getAsJsonObject().get("secret") == null) {
            throw new IOException("Bad response from web server. Expected secret but did not receive one.");
        }

        return response.content.getAsJsonObject().get("secret").getAsString();
    }

    /**
     * Send HTTP request to the session server.
     * @param secret Handshake secret to be included in the server hash
     * @throws IOException when status code is not equal to 204
     */
    public void sendSessionServerRequest(final String secret) throws IOException {
        try {
            final String serverHash = hexDigest(secret + getCompliantUuid());

            final HttpPost post = new HttpPost("https://sessionserver.mojang.com/session/minecraft/join");
            final JsonObject body = createBody(serverHash);
            final StringEntity entity = new StringEntity(body.toString());

            post.setEntity(entity);
            post.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = (CloseableHttpResponse) Quickplay.INSTANCE.requestFactory.
                    httpClient.execute(post);

            if(response.getStatusLine().getStatusCode() != 204)
                throw new IOException("Invalid response of " + response.getStatusLine().getStatusCode() + " from " +
                        "sessions server. Expected: 204");

        } catch (UnsupportedEncodingException e) {
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
     * Get a random salt for the hashing of the "serverHash"
     * @return 32-byte salt
     */
    public String getSalt() {
        byte[] seed = new byte[32];
        csprng.nextBytes(seed);

        // Convert bytes into hexadecimal string
        // https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
        char[] hexChars = new char[seed.length * 2];
        for (int j = 0; j < seed.length; j++) {
            int v = seed[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
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
}
