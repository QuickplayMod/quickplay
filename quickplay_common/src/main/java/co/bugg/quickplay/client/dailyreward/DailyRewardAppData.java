package co.bugg.quickplay.client.dailyreward;

import com.google.gson.JsonElement;

/**
 * Basic appdata for a Daily Reward
 * Typically retrieved from JS window var assignment
 */
public class DailyRewardAppData {

    public String error;
    public DailyRewardOption[] rewards;
    public boolean skippable;
    public JsonElement dailyStreak;
    public JsonElement ad;
    public String id;
    public int activeAd;
}
