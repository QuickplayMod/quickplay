package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.ReflectionUtil;
import co.bugg.quickplay.util.ServerChecker;
import com.google.gson.Gson;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;

public class HttpRequestFactory {

    public HttpClient httpClient;

    public HttpRequestFactory() {
        httpClient = newClient();
    }

    public HttpClient newClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        return HttpClients.custom()
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
            URIBuilder builder = new URIBuilder(endpoint);

            HttpPost post = new HttpPost(builder.toString());

            if(params != null) {
                // Add the parameters to POST body in JSON format
                post.setEntity(new StringEntity(WebResponse.GSON.toJson(params), ContentType.APPLICATION_JSON));
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

        if(Quickplay.INSTANCE.usageStats.sendUsageStats)
            addStatisticsParameters(params);

        return newRequest("https://bugg.co/quickplay/mod/enable", params);
    }

    public Request newExceptionRequest(Exception e) {
        HashMap<String, String> params = new HashMap<>();
        addStatisticsParameters(params);

        params.put("error_message", e.getMessage());
        params.put("stack_trace", Arrays.toString(e.getStackTrace()));

        return newRequest("https://bugg.co/quickplay/mod/exception", params);
    }

    /**
     * Add the default debugging parameters
     * to the provided HashMap
     * @param params HashMap to add to
     */
    public void addStatisticsParameters(HashMap<String, String> params) {
        params.put("token", Quickplay.INSTANCE.usageStats.statsToken.toString()); // Unique token users can use to link their data to themselves
        params.put("manager", Reference.MOD_NAME + " v" + Reference.VERSION); // manager of this data, who sent it (e.g. Quickplay, HCC)
        params.put("version", Reference.VERSION);
        params.put("enabled", String.valueOf(Quickplay.INSTANCE.enabled));
        params.put("currentIP", ServerChecker.getCurrentIP());
        params.put("onHypixel", String.valueOf(Quickplay.INSTANCE.onHypixel));
        params.put("javaVersion", System.getProperty("java.version"));
        params.put("os", System.getProperty("os.name"));
        params.put("osVersion", System.getProperty("os.version"));
        params.put("osArch", System.getProperty("os.arch"));
        // Add a JSON list of all registered mods names
        params.put("installedMods", new Gson().toJson(Loader.instance().getModList()
                .stream().map(ModContainer::getName).toArray()));

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
