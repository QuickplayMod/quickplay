package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.gui.QuickplayGuiPartyEditor;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command for the Quickplay Party system
 */
public class SubCommandParty extends ASubCommand {

    public SubCommandParty(ACommand parent) {
        super(parent, "party", I18n.format("quickplay.commands.quickplay.party.help"), "[launch]", true, true, 88);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final List<String> list = new ArrayList<>();
        if(args.length < 2)
            list.add("launch");
        return list;
    }

    @Override
    public void run(String[] args) {
        if(args.length == 0) {
            new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiPartyEditor()), 1);
        } else {
            if(args[0].equals("launch")) {
                new TickDelay(() -> Quickplay.INSTANCE.threadPool.submit(Quickplay.INSTANCE::launchPartyMode), 1);
            } else {
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.party.syntax", "/qp party launch").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            }
        }
    }
}
