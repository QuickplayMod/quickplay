package co.bugg.quickplay;

import co.bugg.quickplay.client.gui.InstanceDisplay;
import co.bugg.quickplay.util.ServerChecker;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Main event handler for Quickplay
 */
public class QuickplayEventHandler {

    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        new ServerChecker((onHypixel, ip, method) -> {
            Quickplay.INSTANCE.onHypixel = onHypixel;
            Quickplay.INSTANCE.verificationMethod = method;

            // TODO: Send the IP to webhost & verification method
        });
    }

    @SubscribeEvent
    public void onLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Quickplay.INSTANCE.onHypixel = false;
        Quickplay.INSTANCE.verificationMethod = null;
    }


    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if(Quickplay.INSTANCE.onHypixel && event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            Quickplay.INSTANCE.instanceDisplay.render();
        }
    }
}
