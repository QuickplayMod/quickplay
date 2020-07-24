package co.bugg.quickplay.client.dailyreward;

import co.bugg.quickplay.Reference;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.text.DecimalFormat;

/**
 * Class for rewards in the Daily Reward GUI, typically there's three rewards to pick from
 */
public class DailyRewardOption {
    /**
     * Game that this item is for, if applicable (untranslated)
     */
    public String gameType;
    /**
     * Package information for things like Housing blocks
     *
     * Original source has this as <code>package</code> but it is refactored in {@link DailyRewardParser}.
     *
     * Housing packages need to be refactored before translating. Source appdata contains
     * <code>specialoccasion_reward_card_skull_</code> at the beginning of the translation key
     * which needs to be replaced with <code>housing.skull.</code>.
     */
    public String packageInfo;
    /**
     * Number of items
     */
    public int amount;
    /**
     * Rarity of this item
     */
    public String rarity;
    /**
     * Reward name (untranslated)
     *
     * If this is <code>add_vanity</code> then extra work needs to be done to translate
     */
    public String reward;
    /**
     * Key for vanity items
     *
     * Everything before first underscore is the category while everything after is the name
     * Current known categories are: suit; emote; gesture;
     */
    public String key;
    // There is also the "intlist" property, an array of ints. Purpose unclear so unused at the moment.

    /**
     * Whether this option has been uncovered in the GUI yet
     * Defaults as <code>false</code> until the user interacts with it in {@link DailyRewardGui}
     */
    public transient boolean hidden = true;

    /**
     * Get {@link #amount} formatted with commas and decimals
     * @return Formatted amount
     */
    public String getFormattedAmount() {
        if(amount != 0) {
            final DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(amount);
        } else
            return "";
    }

    /**
     * Get the translated {@link #rarity} name
     * @param i18n General translations
     * @return Translated rarity
     */
    public String translateRarity(JsonObject i18n) {
        if(i18n == null)
            return this.rarity;

        String str = String.valueOf(rarity).toLowerCase();
        final JsonElement obj =  i18n.get("rarity." + str);

        if(obj != null)
            str = obj.getAsString();
        if(str != null && gameType != null) {
            str = str.replace("{$game}", gameType);
        }
        return str;
    }

    /**
     * Get the translated {@link #reward} name
     * @param i18n General translations
     * @return Translated reward name
     */
    public String translateReward(JsonObject i18n) {
        if(i18n == null)
            return this.reward;

        String str = this.reward;
        if(reward.equals("add_vanity")) {
            final String[] splitKey = key.split("_");
            final JsonElement obj = i18n.get("vanity." + splitKey[0] + "_" + splitKey[1]);
            if(obj != null)
                str = obj.getAsString();
        } else {
            str = reward.toLowerCase();
            final JsonElement obj = i18n.get("type." + str);
            if(obj != null)
                str = obj.getAsString();
        }

        if(str != null && gameType != null) {
            str = str.replace("{$game}", gameType);
        }
        return str;
    }

    /**
     * Get the {@link #reward} description
     * The reward description is the text that should appear when you hover over this option.
     * @param i18n General translations
     * @return Translated reward description
     */
    public String getRewardDescription(JsonObject i18n) {
        if(i18n == null)
            return "";

        String str = "";
        boolean defaultStr = false; // Whether default string should be grabbed instead of vanity string

        if(reward.equals("add_vanity")) {
            final String[] splitKey = key.split("_");

            String vanityKey = null;
            switch(splitKey[0]) {
                case "suit":
                    vanityKey = "vanity.suits.description";
                    break;
                case "emote":
                    vanityKey = "vanity.emotes.description";
                    break;
                case "gesture":
                    vanityKey = "vanity.gestures.description";
                    break;
            }

            if(vanityKey != null) {
                final JsonElement obj = i18n.get(vanityKey);
                if(obj != null)
                    str = obj.getAsString();
            } else {
                defaultStr = true;
            }
        } else
            defaultStr = true;

        if(defaultStr) {
            str = reward.toLowerCase();
            final JsonElement obj = i18n.get("type." + str + ".description");
            if(obj != null)
                str = obj.getAsString();
        }

        if( gameType != null) {
            str = str.replace("{$game}", gameType);
        }
        return str;
    }

    /**
     * Translate the package information
     * Package information typically contains extra information besides the name, e.g. what Housing block it's for
     * @see #packageInfo
     * @param i18n General translations
     * @return Translated package information
     */
    public String translatePackageInfo(JsonObject i18n) {
        if(i18n == null)
            return "";

        String str = "";

        if(reward.equals("add_vanity")) {
            final String[] splitKey = key.split("_");
            if(splitKey.length > 2) { // Subtype info
                final JsonElement obj = i18n.get("vanity.armor." + splitKey[2]);
                if(obj != null)
                    str = obj.getAsString();
            }
        } else {
            if(packageInfo != null) {
                str = packageInfo.toLowerCase();
                str = str.replace("specialoccasion_reward_card_skull_", "housing.skull.");

                final JsonElement obj = i18n.get(str);
                if (obj != null)
                    str = obj.getAsString();
            }
        }

        if(str != null && gameType != null) {
            str = str.replace("{$game}", gameType);
        }
        return str;
    }

    /**
     * "Show" the option/card in GUIs by setting {@link #hidden} to false and playing the proper sound
     */
    public void show() {
        hidden = false;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(Reference.MOD_ID,"card.turn." + String.valueOf(rarity).toLowerCase()), 1.0F));
    }

    /**
     * Get the texture for this reward option based off of its rarity & hidden state
     * @return The appropriate texture
     */
    public ResourceLocation getTexture() {
        String path;
        if(hidden)
            path = "textures/card-back.png";
        else {
            switch(String.valueOf(rarity).toLowerCase()) {
                default:
                case "common":
                    path = "textures/card-common.png";
                    break;
                case "rare":
                    path = "textures/card-rare.png";
                    break;
                case "epic":
                    path = "textures/card-epic.png";
                    break;
                case "legendary":
                    path = "textures/card-legendary.png";
                    break;
            }
        }

        return new ResourceLocation(Reference.MOD_ID, path);
    }
}
