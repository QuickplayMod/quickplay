package co.bugg.quickplay.wrappers.chat;

import net.minecraft.event.ClickEvent;

import java.io.Serializable;

public class ClickEventWrapper implements Serializable {

    Action action;
    String value;

    public ClickEventWrapper(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public ClickEvent get() {
        return new ClickEvent(ClickEvent.Action.getValueByCanonicalName(this.action.canonicalName), this.value);
    }

    public enum Action
    {
        OPEN_URL("open_url"),
        OPEN_FILE("open_file"),
        RUN_COMMAND("run_command"),
        TWITCH_USER_INFO("twitch_user_info"),
        SUGGEST_COMMAND("suggest_command"),
        CHANGE_PAGE("change_page");

        private final String canonicalName;

        Action(String canonicalNameIn) {
            this.canonicalName = canonicalNameIn;
        }
    }
}
