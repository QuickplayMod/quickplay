package co.bugg.quickplay.wrappers.chat;

import co.bugg.quickplay.Quickplay;
import com.google.gson.*;
import net.minecraft.util.IChatComponent;

import java.io.Serializable;
import java.lang.reflect.Type;

public interface IChatComponentWrapper extends Serializable {

    IChatComponentWrapper setStyle(ChatStyleWrapper style);

    ChatStyleWrapper getStyle();

    /**
     * Appends the given text to the end of this component.
     */
    IChatComponentWrapper appendText(String text);

    /**
     * Appends the given component to the end of this one.
     */
    IChatComponentWrapper appendSibling(IChatComponentWrapper component);

    /**
     * Get the text of this component, <em>and all child components</em>, with all special formatting codes removed.
     */
    String getUnformattedText();

    /**
     * Gets the text of this component, with formatting codes added for rendering.
     */
    String getFormattedText();

    /**
     * Get the Minecraft IChatComponent that this object is wrapping for.
     */
    IChatComponent get();

    static IChatComponentWrapper deserialize(String json) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(IChatComponentWrapper.class, new Deserializer<>()).create();
        return gson.fromJson(json, IChatComponentWrapper.class);
    }

    static String serialize(IChatComponentWrapper component) {
        return new Gson().toJson(component);
    }

    class Deserializer<T extends IChatComponentWrapper> implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonElement className = json.getAsJsonObject().get("srcClass");
            if(className == null) {
                return null;
            }
            try {
                final Class<?> clazz = Class.forName(className.getAsString());
                return context.deserialize(json, clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
                return null;
            }
        }
    }

}
