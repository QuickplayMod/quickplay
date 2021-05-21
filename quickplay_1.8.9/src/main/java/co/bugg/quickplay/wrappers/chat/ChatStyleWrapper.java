package co.bugg.quickplay.wrappers.chat;

import net.minecraft.util.ChatStyle;

import java.io.Serializable;

public class ChatStyleWrapper implements Serializable {

    private ChatStyleWrapper parent = null;
    private Formatting color = null;
    private boolean isBold = false;
    private boolean isObfuscated = false;
    private boolean isItalic = false;
    private boolean isUnderlined = false;
    private boolean isStrikethrough = false;
    private ClickEventWrapper clickEvent;
    private HoverEventWrapper hoverEvent;

    public ChatStyleWrapper apply(Formatting formattingValue) {
        if (formattingValue == Formatting.ITALIC) {
            this.isItalic = !this.isItalic;
        } else if (formattingValue == Formatting.BOLD) {
            this.isBold = !this.isBold;
        } else if (formattingValue == Formatting.OBFUSCATED) {
            this.isObfuscated = !this.isObfuscated;
        } else if (formattingValue == Formatting.UNDERLINE) {
            this.isUnderlined = !this.isUnderlined;
        } else if (formattingValue == Formatting.STRIKETHROUGH) {
            this.isStrikethrough = !this.isStrikethrough;
        } else if (formattingValue == Formatting.RESET) {
            this.reset();
        } else {
            this.color = formattingValue;
        }
        return this;
    }

    public ChatStyleWrapper reset() {
        this.isBold = false;
        this.isObfuscated = false;
        this.isItalic = false;
        this.isUnderlined = false;
        this.isStrikethrough = false;
        this.color = null;
        return this;
    }

    public ChatStyleWrapper setHoverEvent(HoverEventWrapper hoverEventWrapper) {
        this.hoverEvent = hoverEventWrapper;
        return this;
    }
    public ChatStyleWrapper setClickEvent(ClickEventWrapper clickEventWrapper) {
        this.clickEvent = clickEventWrapper;
        return this;
    }

    public ChatStyleWrapper setParent(ChatStyleWrapper csb) {
        this.parent = csb;
        return this;
    }

    public ChatStyle get() {
        final ChatStyle style = new ChatStyle();

        if(this.color != null) {
            style.setColor(this.color.convert());
        }

        style.setUnderlined(this.isUnderlined);
        style.setBold(this.isBold);
        style.setItalic(this.isItalic);
        style.setObfuscated(this.isObfuscated);
        style.setStrikethrough(this.isStrikethrough);

        if(this.clickEvent != null) {
            style.setChatClickEvent(this.clickEvent.get());
        }
        if(this.hoverEvent != null) {
            style.setChatHoverEvent(this.hoverEvent.get());
        }
        if(this.parent != null) {
            style.setParentStyle(this.parent.get());
        }
        return style;
    }

}
