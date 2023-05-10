package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.util.ReflectionUtil;
import co.bugg.quickplay.util.ServerChecker;
import com.google.gson.Gson;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Factory for Quickplay HTTP requests
 */
public class HttpRequestFactory {

    public HttpClient httpClient;

    public HttpRequestFactory() {
        try {
            httpClient = newClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient newClient() throws IOException {

        try {
            SSLContext sslCtx = this.createSslContext();

            // noinspection VulnerableCodeUsages -- Does not affect clients
            final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslCtx);
            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build()
            );
            connectionManager.setMaxTotal(100);

            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setUserAgent("Minecraft Mod " + Reference.MOD_NAME + " - " + Reference.VERSION)
                    .build();
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Root CA registration failed", e);
        }
    }

    /**
     * Basic web request to the AnimatedCrosshair web request API
     * Should be executed in its own thread!
     * @param endpoint Endpoint to request to
     * @param params GET parameters to request with
     * @return Response from the website
     */
    public Request newRequest(String endpoint, HashMap<String, String> params) {

        try {
            URIBuilder builder = new URIBuilder(endpoint);

            HttpPost post = new HttpPost(builder.toString());

            if(params != null) {
                // Add the parameters to POST body in JSON format
                post.setEntity(new StringEntity(new Gson().toJson(params), ContentType.APPLICATION_JSON));
                post.addHeader("Content-Type", "application/json");
            }

            return new Request(post, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }

        return null;
    }

    public Request newEnableRequest() {
        HashMap<String, String> params = new HashMap<>();
        addStatisticsParameters(params);

        return newRequest("https://bugg.co/quickplay/mod/enable", params);
    }

    public Request newExceptionRequest(Exception e) {
        HashMap<String, String> params = new HashMap<>();
        addStatisticsParameters(params);

        String message = e.getMessage();
        if(message == null) {
            message = "<null>";
        }

        params.put("error_message", message);
        params.put("stack_trace", Arrays.toString(e.getStackTrace()));

        return newRequest("https://bugg.co/quickplay/mod/exception", params);
    }

    public Request newPingRequest() {
        HashMap<String, String> params = new HashMap<>();
        addStatisticsParameters(params);

        return newRequest("https://bugg.co/quickplay/mod/ping", params);
    }

    /**
     * Add the default debugging parameters
     * to the provided HashMap
     * @param params HashMap to add to
     */
    public void addStatisticsParameters(HashMap<String, String> params) {
        // These values are always sent regardless of usage stats setting
        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null) {
            params.put("token", Quickplay.INSTANCE.usageStats.statsToken.toString()); // Unique token users can use to link their data to themselves
        }
        params.put("manager", Reference.MOD_NAME + " v" + Reference.VERSION); // manager of this data, who sent it (e.g. Quickplay, HCC)
        params.put("version", Reference.VERSION);
        // Tells the web server if the client wants to be notified of any new updates
        params.put("updateNotifications", Boolean.toString(Quickplay.INSTANCE.settings != null && Quickplay.INSTANCE.settings.updateNotifications));

        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.sendUsageStats) {
            final Gson gson = new Gson();
            params.put("enabled", String.valueOf(Quickplay.INSTANCE.enabled));
            params.put("currentIP", ServerChecker.getCurrentIP());
            params.put("onHypixel", String.valueOf(Quickplay.INSTANCE.onHypixel));
            params.put("hypixelVerificationMethod", String.valueOf(Quickplay.INSTANCE.verificationMethod));
            params.put("javaVersion", System.getProperty("java.version"));
            params.put("os", System.getProperty("os.name"));
            params.put("osVersion", System.getProperty("os.version"));
            params.put("osArch", System.getProperty("os.arch")); // OS Architecture
            // Add a JSON list of all registered mods names
            params.put("installedMods", gson.toJson(Loader.instance().getModList()
                    .stream().map(ModContainer::getName).toArray()));
            // Add settings
            if(Quickplay.INSTANCE.settings != null) {
                params.put("settings", gson.toJson(Quickplay.INSTANCE.settings));
            }
            if(Quickplay.INSTANCE.keybinds != null) {
                params.put("keybinds", gson.toJson(Quickplay.INSTANCE.keybinds));
            }

            try {
                params.put("mcVersion", ReflectionUtil.getMCVersion());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
                params.put("mcVersion", "unknown");
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }

            try {
                params.put("forgeVersion", ReflectionUtil.getForgeVersion());
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
                params.put("forgeVersion", "unknown");
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
        }
    }

    /**
     * License CC BY-SA 4.0 Jan Novotný and Erik Roberts
     * <a href="https://blog.novoj.net/posts/2016-02-29-how-to-make-apache-httpclient-trust-lets-encrypt-certificate-authority/">Source</a>
     * <a href="https://creativecommons.org/licenses/by-sa/4.0/">License</a>
     */
    private SSLContext createSslContext() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        try(final InputStream is = this.getClass().getClassLoader().getResourceAsStream("certs/cacerts.jks")) {
            if(is == null) {
                throw new IOException("CA certificates keystore not found");
            }
            ks.load(is, "changeit".toCharArray());
        }

        final TrustManagerFactory defaultTm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        final TrustManagerFactory customTm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        defaultTm.init((KeyStore)null);
        customTm.init(ks);

        final TrustManager[] trustManagers = new TrustManager[1];
        trustManagers[0] = new TrustManagerDelegate(
                (X509TrustManager) customTm.getTrustManagers()[0],
                (X509TrustManager) defaultTm.getTrustManagers()[0]
        );

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }
}
