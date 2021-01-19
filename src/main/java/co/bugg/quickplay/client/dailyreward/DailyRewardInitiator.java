package co.bugg.quickplay.client.dailyreward;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.actions.serverbound.GetDailyRewardAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles initiating the daily reward claiming process.
 */
public class DailyRewardInitiator {

    /**
     * Since event cancelling doesn't seem to work in {@link this#guiOpened(GuiOpenEvent)}, I've opted to
     * simply save the GUI in this field and change the GUI to be displayed from a book to the loading GUI
     */
    DailyRewardGuiLoading overrideBookGui;

    @SubscribeEvent
    public void guiOpened(GuiOpenEvent event) {
        if(event.gui instanceof GuiScreenBook) {
            event.gui = this.overrideBookGui;
        }
        if(event.gui == null) { // If all GUIs are closed then stop listening
            Quickplay.INSTANCE.unregisterEventHandler(this);
        }
    }
    /**
     * Constructor
     *
     * @param code Daily reward code
     */
    public DailyRewardInitiator(String code) {

        this.overrideBookGui = new DailyRewardGuiLoading();
        Quickplay.INSTANCE.registerEventHandler(this);
        QuickplayEventHandler.mainThreadScheduledTasks.add(() ->
                Minecraft.getMinecraft().displayGuiScreen(this.overrideBookGui));

        try {
            Quickplay.INSTANCE.socket.sendAction(new GetDailyRewardAction(code));
        } catch (ServerUnavailableException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }
}
