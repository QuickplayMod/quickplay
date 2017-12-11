package co.bugg.quickplay.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class WebResponse {
    public static final Gson GSON = new Gson();

    public boolean ok;
    public List<ResponseAction> actions = new ArrayList<>();
    public JsonElement content;

    public static WebResponse fromJson(String json) {
        return GSON.fromJson(json, WebResponse.class);
    }
}
