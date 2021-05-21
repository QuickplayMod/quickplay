package co.bugg.quickplay.wrappers.chat;

import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class ChatComponentTranslationWrapper implements IChatComponentWrapper {

    String srcClass;
    String text;
    Object[] args;
    ChatStyleWrapper style = new ChatStyleWrapper();
    List<IChatComponentWrapper> siblings = new ArrayList<>();

    public ChatComponentTranslationWrapper(String str, Object... args) {
        this.srcClass = this.getClass().getName();
        this.text = str;
        this.args = args;
        for (Object object : args)
        {
            if (object instanceof IChatComponentWrapper)
            {
                ((IChatComponentWrapper)object).getStyle().setParent(this.getStyle());
            }
        }
    }

    public IChatComponentWrapper setStyle(ChatStyleWrapper style) {
        if(style == null) {
            style = new ChatStyleWrapper();
        }
        this.style = style;

        for (IChatComponentWrapper sibling : siblings) {
            sibling.getStyle().setParent(style);
        }
        for (Object object : this.args)
        {
            if (object instanceof IChatComponentWrapper)
            {
                ((IChatComponentWrapper)object).getStyle().setParent(this.getStyle());
            }
        }
        return this;
    }

    @Override
    public ChatStyleWrapper getStyle() {
        return this.style;
    }

    public ChatComponentTranslation get() {
        final ChatComponentTranslation component = new ChatComponentTranslation(this.text);
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
