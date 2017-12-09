package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.ReflectionUtil;
import co.bugg.quickplay.util.ServerChecker;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestFactory {

    public HttpClient httpClient;

    public HttpRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setUserAgent("Minecraft Mod " + Reference.MOD_NAME + " - " + Reference.VERSION)
                .build();
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
            URIBuilder builder = new URIBuilder("https://bugg.co/quickplay/mod/" + endpoint);

            HttpPost post = new HttpPost(builder.toString());
            // Add the parameters to POST body in JSON format
            post.setEntity(new StringEntity(WebResponse.GSON.toJson(params), ContentType.APPLICATION_JSON));
            post.addHeader("Content-Type", "application/json");

            return new Request(post, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Request newEnableRequest(HashMap<String, String> params) {
        return newRequest("enable", params);
    }

    /**
     * Add the default debugging parameters
     * to the provided HashMap
     * @param params HashMap to add to
     */
    public void addDebuggingParameters(HashMap<String, String> params) {
        // Used for debugging purposes, to prevent the issue of
        // consistently asking users to post screenshots of "/qp debug"
        params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());
        params.put("version", Reference.VERSION);
        params.put("enabled", String.valueOf(Quickplay.INSTANCE.enabled));
        params.put("currentIP", ServerChecker.getCurrentIP());
        params.put("onHypixel", String.valueOf(Quickplay.INSTANCE.onHypixel));
        params.put("javaVersion", System.getProperty("java.version"));
        params.put("os", System.getProperty("os.name"));
        params.put("osVersion", System.getProperty("os.version"));
        params.put("osArch", System.getProperty("os.arch"));
        // Add a JSON list of all registered mods names
        params.put("installedMods", WebResponse.GSON.toJson(Loader.instance().getModList()
                .stream().map(ModContainer::getName).toArray()));

        try {
            params.put("mcVersion", ReflectionUtil.getMCVersion());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            params.put("mcVersion", "unknown");
        }

        try {
            params.put("forgeVersion", ReflectionUtil.getForgeVersion());
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            params.put("forgeVersion", "unknown");
        }
    }
}
