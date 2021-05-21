package co.bugg.quickplay.wrappers;

import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.buffer.ChatBuffer;
import co.bugg.quickplay.util.buffer.MessageBuffer;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class MinecraftWrapper {

    /**
     * Minecraft instance
     */
    final Minecraft mc;
    /**
     * Buffer for sending messages to the client
     */
    public MessageBuffer messageBuffer;
    /**
     * Buffer for sending chat messages to the server
     */
    public ChatBuffer chatBuffer;

    public MinecraftWrapper() {
        this.mc = Minecraft.getMinecraft();
    }

    public UUID getUuid() {
        if(this.mc.getSession() == null || this.mc.getSession().getProfile() == null) {
            return null;
        }
        return this.mc.getSession().getProfile().getId();
    }

    public void sendLocalMessage(Message msg) {
        this.messageBuffer.push(msg);
    }

    public void sendLocalMessageDirect(Message msg) {
        if(this.mc.thePlayer == null) {
            return;
        }
        this.mc.thePlayer.addChatMessage(msg.getMessage().get());
    }

    public void sendRemoteMessage(String msg) {
        this.chatBuffer.push(msg);
    }

    public void sendRemoteMessageDirect(String msg) {
        if(this.mc.thePlayer == null) {
            return;
        }
        this.mc.thePlayer.sendChatMessage(msg);
    }

    public void openGui(QuickplayGui gui) {
        this.mc.displayGuiScreen(gui);
    }

    public int getCharWidth(char c) {
        return this.mc.fontRendererObj.getCharWidth(c);
    }

    public int getChatWindowWidth() {
        return this.mc.ingameGUI.getChatGUI().getChatWidth();
    }
}
