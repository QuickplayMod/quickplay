package co.bugg.quickplay.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A response from the backend web server.
 */
public class WebResponse {
    /**
     * Whether the request was valid & handled successfully
     *
     * It's protocol to execute {@link #actions} regardless of this value,
     * although anything to do with {@link #content} should be ignored
     * (with the exception of the <code>error</code> field, which says what went wrong)
     *
     */
    public boolean ok;
    /**
     * The list of instructions to execute by the web server.
     * Actions should be ran regardless of {@link #ok}.
     *
     * @see ResponseActionType
     */
    public List<ResponseAction> actions = new ArrayList<>();
    /**
     * Any other content relating to the request.
     *
     * e.g. for game list requests, this should contain the
     * field <code>games</code>, which will contain a list of all games, modes,
     * and buttons available for each {@link co.bugg.quickplay.games.Game}.
     *
     * <code>error</code> will contain an error message if {@link #ok} is <code>false</code>.
     */
    public JsonElement content;

    /**
     * Deserializes a JSON response from the web server into an
     * instance of {@link this}
     * @param json JSON string to deserialize
     * @return Instance of {@link WebResponse} that {@param json} represented
     */
    public static WebResponse fromJson(String json) {
        return new Gson().fromJson(json, WebResponse.class);
    }
}
