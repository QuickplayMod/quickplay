package co.bugg.quickplay.command;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.List;

public abstract class ASubCommand {

    public ASubCommand(ASubCommandParent parent, String name, String helpMessage, boolean displayInHelpMenu, boolean displayInTabList, double priority) {
        this.parent = parent;
        this.name = name;
        this.helpMessage = helpMessage;
        this.displayInHelpMenu = displayInHelpMenu;
        this.displayInTabList = displayInTabList;
        this.priority = priority;
    }
    private ASubCommandParent parent;
    private String name;
    private String helpMessage;
    private boolean displayInHelpMenu;
    private boolean displayInTabList;
    private double priority;

    public abstract List<String> getTabCompletions();
    public abstract void run(String[] args);

    public ASubCommandParent getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public IChatComponent getFormattedHelpMessage() {
        final String fullCommand = "/" + parent.getCommandName() + " " + getName();

        final IChatComponent msg = new ChatComponentText("");

        final IChatComponent command = new ChatComponentText(fullCommand);
        command.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA));

        final IChatComponent separator = new ChatComponentText(" - ");
        separator.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));

        final IChatComponent helpMessage = new ChatComponentText(getHelpMessage());
        helpMessage.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));

        msg.appendSibling(command);
        msg.appendSibling(separator);
        msg.appendSibling(helpMessage);

        final ChatStyle msgStyle = new ChatStyle();
        msgStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand));

        final IChatComponent hoverText = new ChatComponentText("Click to put " + fullCommand + " in chat box");
        final ChatStyle hoverStyle = new ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
        hoverText.setChatStyle(hoverStyle);

        msgStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        msg.setChatStyle(msgStyle);

        return msg;
    }

    public boolean canDisplayInHelpMenu() {
        return displayInHelpMenu;
    }

    public boolean canDisplayInTabList() {
        return displayInTabList;
    }

    public double getPriority() {
        return priority;
    }
}
