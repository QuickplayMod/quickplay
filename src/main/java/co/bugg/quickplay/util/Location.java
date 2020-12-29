package co.bugg.quickplay.util;

import com.google.gson.Gson;

/**
 * Class matching the response of the /locraw message.
 */
public class Location {
    public String server = null;
    public String gametype = null;
    public String mode = null;
    public String map = null;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Location)) {
            return false;
        }
        final Location loc2 = (Location) obj;
        return this.toString().equals(loc2.toString());
    }
}
