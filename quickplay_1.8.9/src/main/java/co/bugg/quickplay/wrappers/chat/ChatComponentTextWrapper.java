package co.bugg.quickplay.wrappers.chat;

import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

public class ChatComponentTextWrapper implements IChatComponentWrapper {

    String srcClass;
    String text;
    ChatStyleWrapper style = new ChatStyleWrapper();
    List<IChatComponentWrapper> siblings = new ArrayList<>();

    public ChatComponentTextWrapper(String str) {
        this.srcClass = this.getClass().getName();
        this.text = str;
    }

    public IChatComponentWrapper setStyle(ChatStyleWrapper style) {
        if(style == null) {
            style = new ChatStyleWrapper();
        }
        this.style = style;

        for (IChatComponentWrapper sibling : siblings) {
            sibling.getStyle().setParent(style);
        }
        return this;
    }

    @Override
    public ChatStyleWrapper getStyle() {
        return this.style;
    }

    public ChatComponentText get() {
        final ChatComponentText component = new ChatComponentText(this.text);
        component.setChatStyle(this.style.get());
        for(IChatComponentWrapper sibling : this.siblings) {
            component.appendSibling(sibling.get());
        }
        return component;
    }

    @Override
    public IChatComponentWrapper appendText(String text) {
        this.appendSibling(new ChatComponentTextWrapper(text));
        return this;
    }

    @Override
    public IChatComponentWrapper appendSibling(IChatComponentWrapper passed) {
        passed.getStyle().setParent(this.getStyle());
        this.siblings.add(passed);
        return this;
    }

    @Override
    public String getUnformattedText() {
        return this.get().getUnformattedText();
    }

    @Override
    public String getFormattedText() {
        return this.get().getFormattedText();
    }
}
