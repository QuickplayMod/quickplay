package dev.ecr.commands;

import dev.ecr.QuickplayConstants;
import dev.ecr.gui.QuickplayMainGui;
import dev.ecr.util.TickDelay;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.wrappers.UPlayer;
import gg.essential.universal.wrappers.message.UTextComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class QuickplayCommand extends CommandBase {

    private final HashMap<String, String> helpTexts = new HashMap<>();

    public QuickplayCommand() {
        super();
        this.helpTexts.put("help", "Show this menu");
        this.helpTexts.put("about", "View information about your current Quickplay installation");
    }

    @Override
    public String getCommandName() {
        return "quickplay";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>() {{
            add("qp");
        }};
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/quickplay <about | help>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            new TickDelay(() -> UMinecraft.setCurrentScreenObj(new QuickplayMainGui()), 1);
            return;
        }

        switch (args[0]) {
            case "help":
                UTextComponent help = new UTextComponent("");
                help.appendSibling(
                        new UTextComponent("Quickplay Help\n")
                                .setChatStyle(new ChatStyle()
                                        .setColor(EnumChatFormatting.DARK_AQUA)
                                        .setBold(true)
                                )
                );
                help.appendSibling(
                        new UTextComponent("Commands: \n")
                                .setChatStyle(new ChatStyle()
                                        .setColor(EnumChatFormatting.GREEN)
                                )
                );

                for (Map.Entry<String, String> entry : this.helpTexts.entrySet()) {
                    UTextComponent helpLine = new UTextComponent("");
                    final String command = String.format("/quickplay %s", entry.getKey());
                    helpLine.appendSibling(
                            new UTextComponent(command)
                                    .setChatStyle(new ChatStyle()
                                            .setColor(EnumChatFormatting.GREEN)
                                            .setBold(true)
                                    )
                    );
                    helpLine.appendSibling(
                            new UTextComponent(" - ")
                                    .setChatStyle(new ChatStyle()
                                            .setColor(EnumChatFormatting.GRAY)
                                            .setBold(false)
                                    )
                    );
                    helpLine.appendSibling(
                            new UTextComponent(entry.getValue())
                                    .setChatStyle(new ChatStyle()
                                            .setColor(EnumChatFormatting.WHITE)
                                            .setBold(false)
                                    )
                    );
                    helpLine.appendText("\n");
                    helpLine.setChatStyle(new ChatStyle()
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new UTextComponent("Click to use")))
                    );
                    help.appendSibling(helpLine);
                }

                UPlayer.sendClientSideMessage(help);
                break;
            case "about":
                UTextComponent about = new UTextComponent("");
                about.appendSibling(new UTextComponent("About Quickplay\n")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.DARK_AQUA)
                                .setBold(true)
                        )
                );

                about.appendSibling(new UTextComponent("Version: ")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.GREEN)
                        ));
                about.appendSibling(new UTextComponent(String.format("%s\n", QuickplayConstants.MOD_VERSION))
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.WHITE)
                        ));
                about.appendSibling(new UTextComponent("Website: ")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.GREEN)
                        ));
                about.appendSibling(new UTextComponent("https://quickplay.ecr.dev/\n\n")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.WHITE)
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://quickplay.ecr.dev/"))
                        ));

                about.appendSibling(new UTextComponent("Attributions:\n")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.DARK_GREEN)
                                .setBold(true)
                        ));

                about.appendSibling(new UTextComponent("Elementa - LGPL-3.0\n")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.WHITE)
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/EssentialGG/Elementa/"))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new UTextComponent("Open Homepage")))
                        ));
                about.appendSibling(new UTextComponent("UniversalCraft - LGPL-3.0\n")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.WHITE)
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/EssentialGG/UniversalCraft/"))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new UTextComponent("Open Homepage")))
                        ));

                UPlayer.sendClientSideMessage(about);
                break;
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "about", "help");
        return Collections.emptyList();
    }
}
