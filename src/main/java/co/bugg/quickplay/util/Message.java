package co.bugg.quickplay.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.nio.CharBuffer;

/**
 * Message wrapper for commonly used chat messages
 */
public class Message {
    /**
     * Base of the message being sent
     */
    private final IChatComponent message;
    /**
     * Whether the message should be wrapped in "separator bars"
     */
    private final boolean separators;
    /**
     * Whether this message can be sent even if the mod is disabled
     */
    private final boolean bypassEnabledSetting;

    /**
     * Constructor
     * @param message Message to be sent
     */
    public Message(IChatComponent message) {
        this(message, false, false);
    }

    /**
     * Constructor
     * @param message Message to be sent
     * @param separators Whether the message should be wrapped in separators
     */
    public Message(IChatComponent message, boolean separators) {
        this(message, separators, false);
    }

    /**
     * Constructor
     * @param message Message to be sent
     * @param separators Whether the message should be wrapped in separators
     * @param bypassEnabledSetting Whether the message should bypass the mod "enabled" setting
     */
    public Message(IChatComponent message, boolean separators, boolean bypassEnabledSetting) {
        this.message = message;
        this.separators = separators;
        this.bypassEnabledSetting = bypassEnabledSetting;
    }

    /**
     * Get a chat message that can be sent, with bars included if applicable
     * @return IChatComponent that can be sent in chat
     */
    public IChatComponent getMessage() {
        IChatComponent component = new ChatComponentText("");
        if(separators) component.appendSibling(getMessageSeparator()).appendText("\n");
        component.appendSibling(message);
        if(separators) component.appendText("\n").appendSibling(getMessageSeparator());
        return component;
    }

    /**
     * Getter for whether the message can bypass enabled setting
     * @return {@link #bypassEnabledSetting}
     */
    public boolean canBypassEnabledSetting() {
        return bypassEnabledSetting;
    }

    /**
     * Get the separator that is prepended & appended to messages with "separators" as true
     * The separators are always the width of the clients chat box (without formatting)
     * @return Separator
     */
    public static IChatComponent getMessageSeparator() {
        char separatorChar = I18n.format("quickplay.chat.separator").charAt(0);
        final int chatWidth = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatWidth();
        final int separatorWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(separatorChar);

        final String separatorText = CharBuffer.allocate(chatWidth / separatorWidth).toString().replace('\0', separatorChar);

        final IChatComponent separator = new ChatComponentText(separatorText);
        final ChatStyle separatorStyle = new ChatStyle();
        separatorStyle.setColor(EnumChatFormatting.GOLD);
        separatorStyle.setStrikethrough(true);
        separator.setChatStyle(separatorStyle);

        return separator;
    }

    /**
     * Deserialize a Message object from JSON
     * @param value JSON to be parsed
     * @return new Message
     */
    public static Message fromJson(JsonElement value) {
        JsonObject obj = value.getAsJsonObject();
        return new Message(
                IChatComponent.Serializer.jsonToComponent(obj.get("message").toString()),
                obj.get("separators").getAsBoolean(),
                obj.get("bypassEnabledSetting").getAsBoolean()
        );
    }
}
