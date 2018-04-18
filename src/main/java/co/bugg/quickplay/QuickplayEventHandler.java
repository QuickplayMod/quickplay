package co.bugg.quickplay;

import cc.hyperium.event.*;
import co.bugg.quickplay.client.gui.InstanceDisplay;
import co.bugg.quickplay.client.gui.config.QuickplayGuiUsageStats;
import co.bugg.quickplay.util.ServerChecker;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

import java.util.ArrayList;

/**
 * Main event handler for Quickplay
 */
public class QuickplayEventHandler {

    /**
     * Runnable tasks scheduled to be ran in the main thread
     * This is mainly used for things that need Minecraft's OpenGL context
     * All items in this list are called before a frame is rendered
     */
    public static ArrayList<Runnable> mainThreadScheduledTasks = new ArrayList<>();

    @InvokeEvent
    public void onJoin(ServerJoinEvent event) {
        new ServerChecker((onHypixel, ip, method) -> {
            Quickplay.INSTANCE.onHypixel = onHypixel;
            Quickplay.INSTANCE.verificationMethod = method;
        });
    }

    @InvokeEvent
    public void onLeave(ServerLeaveEvent event) {
        Quickplay.INSTANCE.onHypixel = false;
        Quickplay.INSTANCE.verificationMethod = null;
    }


    @InvokeEvent
    public void onRenderOverlay(RenderHUDEvent event) {
        if(Quickplay.INSTANCE.onHypixel) {
            // Only render overlay if there is no other GUI open at the moment or if the GUI is chat (assuming proper settings)
            if(Quickplay.INSTANCE.settings.displayInstance && (Minecraft.getMinecraft().currentScreen == null ||
                    (Quickplay.INSTANCE.settings.displayInstanceWithChatOpen && (Minecraft.getMinecraft().currentScreen instanceof GuiChat)))) {
                InstanceDisplay instanceDisplay = Quickplay.INSTANCE.instanceDisplay;
                instanceDisplay.render(instanceDisplay.getxRatio(), instanceDisplay.getyRatio(), Quickplay.INSTANCE.settings.instanceOpacity);
            }
        }
    }

    @InvokeEvent
    public void onRender(RenderEvent event) {
        // handle any runnables that need to be ran with OpenGL context
        if(!mainThreadScheduledTasks.isEmpty()) {
            for(Runnable runnable : (ArrayList<Runnable>) mainThreadScheduledTasks.clone()) {
                runnable.run();
                mainThreadScheduledTasks.remove(runnable);
            }
        }
    }

    @InvokeEvent
    public void onWorldLoad(WorldChangeEvent event) {
        // Prompt the user for usage stats setting every time they join a world until they select an
        // option (at which point promptUserForUsageStats is set to false & ConfigUsageStats is created)
        if(Quickplay.INSTANCE.promptUserForUsageStats)
            new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiUsageStats()), 20);
    }
}
