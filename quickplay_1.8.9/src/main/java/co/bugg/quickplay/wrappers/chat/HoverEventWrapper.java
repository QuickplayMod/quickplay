package co.bugg.quickplay.wrappers.chat;

import net.minecraft.event.HoverEvent;

import java.io.Serializable;

public class HoverEventWrapper implements Serializable {

    Action action;
    IChatComponentWrapper component;

    public HoverEventWrapper(Action action, IChatComponentWrapper chatComponent) {
        this.action = action;
        this.component = chatComponent;
    }

    public HoverEvent get() {
        return new HoverEvent(HoverEvent.Action.getValueByCanonicalName(action.canonicalName), component.get());
    }

    public enum Action
    {
        SHOW_TEXT("show_text"),
        SHOW_ACHIEVEMENT("show_achievement"),
        SHOW_ITEM("show_item"),
        SHOW_ENTITY("show_entity");

        private final String canonicalName;

        Action(String canonicalNameIn) {
            this.canonicalName = canonicalNameIn;
        }
    }
}
