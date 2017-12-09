package co.bugg.quickplay.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.nio.CharBuffer;

public class Message {
    private IChatComponent message;
    private boolean separators;
    private boolean bypassEnabledSetting;

    public Message(IChatComponent message) {
        this(message, false, false);
    }

    public Message(IChatComponent message, boolean separators) {
        this(message, separators, false);
    }

    public Message(IChatComponent message, boolean separators, boolean bypassEnabledSetting) {
        this.message = message;
        this.separators = separators;
        this.bypassEnabledSetting = bypassEnabledSetting;
    }

    public IChatComponent getMessage() {
        IChatComponent component = new ChatComponentText("");
        if(separators) component.appendSibling(getMessageSeparator()).appendText("\n");
        component.appendSibling(message);
        if(separators) component.appendText("\n").appendSibling(getMessageSeparator());
        return component;
    }

    public boolean canBypassEnabledSetting() {
        return bypassEnabledSetting;
    }

    public static IChatComponent getMessageSeparator() {
        char separatorChar = new ChatComponentTranslation("quickplay.chat.separator").getUnformattedTextForChat().charAt(0);
        final int chatWidth = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatWidth();
        final int separatorWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(separatorChar);

        final String separatorText = CharBuffer.allocate(chatWidth / separatorWidth).toString().replace('\0', separatorChar);

        final IChatComponent separator = new ChatComponentText(separatorText);
        final ChatStyle separatorStyle = new ChatStyle();
        separatorStyle.setColor(EnumChatFormatting.GOLD);
        separator.setChatStyle(separatorStyle);

        return separator;
    }

    public static Message fromJson(JsonElement value) {
        JsonObject obj = value.getAsJsonObject();
        System.out.println(obj.get("message").toString());
        return new Message(
                IChatComponent.Serializer.jsonToComponent(obj.get("message").toString()),
                obj.get("separators").getAsBoolean(),
                obj.get("bypassEnabledSetting").getAsBoolean()
        );
    }
}
