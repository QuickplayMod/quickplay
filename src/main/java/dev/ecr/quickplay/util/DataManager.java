package dev.ecr.quickplay.util;

import dev.ecr.quickplay.QuickplayConstants;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class DataManager {

    HttpClient httpClient;

    public DataManager() {
        String userAgent = String.format(
                "Quickplay/%s (Minecraft %s %s)",
                QuickplayConstants.MOD_VERSION,
                QuickplayConstants.MC_PLATFORM,
                QuickplayConstants.MC_VERSION
        );
        this.httpClient = HttpClientBuilder.create().setUserAgent(userAgent).build();
    }


}
