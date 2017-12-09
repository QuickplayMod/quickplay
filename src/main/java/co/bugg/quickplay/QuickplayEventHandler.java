package co.bugg.quickplay;

import co.bugg.quickplay.util.ServerChecker;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

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
    public void onChat(ClientChatReceivedEvent event) {
        System.out.println(IChatComponent.Serializer.componentToJson(event.message));
    }
}
