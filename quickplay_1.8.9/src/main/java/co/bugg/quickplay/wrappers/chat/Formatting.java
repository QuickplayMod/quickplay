package co.bugg.quickplay.wrappers.chat;

import net.minecraft.util.EnumChatFormatting;

import java.io.Serializable;

public enum Formatting implements Serializable {
    BLACK("BLACK"),
    DARK_BLUE("DARK_BLUE"),
    DARK_GREEN("DARK_GREEN"),
    DARK_AQUA("DARK_AQUA"),
    DARK_RED("DARK_RED"),
    DARK_PURPLE("DARK_PURPLE"),
    GOLD("GOLD"),
    GRAY("GRAY"),
    DARK_GRAY("DARK_GRAY"),
    BLUE("BLUE"),
    GREEN("GREEN"),
    AQUA("AQUA"),
    RED("RED"),
    LIGHT_PURPLE("LIGHT_PURPLE"),
    YELLOW("YELLOW"),
    WHITE("WHITE"),
    OBFUSCATED("OBFUSCATED"),
    BOLD("BOLD"),
    STRIKETHROUGH("STRIKETHROUGH"),
    UNDERLINE("UNDERLINE"),
    ITALIC("ITALIC"),
    RESET("RESET");

    String name;

    Formatting(String name) {
        this.name = name;
    }

    public EnumChatFormatting convert() {
        return EnumChatFormatting.getValueByName(this.name);
    }

    @Override
    public String toString() {
        return this.convert().toString();
    }
}
