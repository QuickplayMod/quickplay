package co.bugg.quickplay;

import co.bugg.quickplay.actions.serverbound.ServerJoinedAction;
import co.bugg.quickplay.actions.serverbound.ServerLeftAction;
import co.bugg.quickplay.client.dailyreward.DailyRewardInitiator;
import co.bugg.quickplay.client.gui.InstanceDisplay;
import co.bugg.quickplay.client.gui.config.QuickplayGuiUsageStats;
import co.bugg.quickplay.util.ServerChecker;
import co.bugg.quickplay.util.ServerUnavailableException;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        new ServerChecker((onHypixel, ip, method) -> {
            Quickplay.INSTANCE.onHypixel = onHypixel;
            Quickplay.INSTANCE.verificationMethod = method;
        });
        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                // Metadata is currently unused, however it's available in the spec for the future.
                Quickplay.INSTANCE.socket.sendAction(new ServerJoinedAction(ServerChecker.getCurrentIP(), null));
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
            }
        });
    }

    @SubscribeEvent
    public void onLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Quickplay.INSTANCE.onHypixel = false;
        Quickplay.INSTANCE.verificationMethod = null;
        Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                Quickplay.INSTANCE.socket.sendAction(new ServerLeftAction());
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
            }
        });
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if(Quickplay.INSTANCE.onHypixel && event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            // Only render overlay if there is no other GUI open at the moment or if the GUI is chat (assuming proper settings)
            if(Quickplay.INSTANCE.settings.displayInstance && (Minecraft.getMinecraft().currentScreen == null ||
                    (Quickplay.INSTANCE.settings.displayInstanceWithChatOpen && (Minecraft.getMinecraft().currentScreen instanceof GuiChat)))) {
                InstanceDisplay instanceDisplay = Quickplay.INSTANCE.instanceDisplay;
                instanceDisplay.render(instanceDisplay.getxRatio(), instanceDisplay.getyRatio(), Quickplay.INSTANCE.settings.instanceOpacity);
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        // handle any runnables that need to be ran with OpenGL context
        if(event.phase == TickEvent.Phase.START && !mainThreadScheduledTasks.isEmpty()) {
            for(Runnable runnable : (ArrayList<Runnable>) mainThreadScheduledTasks.clone()) {
                runnable.run();
                mainThreadScheduledTasks.remove(runnable);
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        // Prompt the user for usage stats setting every time they join a world until they select an
        // option (at which point promptUserForUsageStats is set to false & ConfigUsageStats is created)
        if(Quickplay.INSTANCE.promptUserForUsageStats) {
            new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new QuickplayGuiUsageStats()), 20);
        }
    }

    /**
     * Regex pattern for the daily reward message
     */
    final Pattern pattern = Pattern.compile("^\\n(?:" +
            "Click the link to visit our website and claim your reward|" +
            "Clica no link para visitares o nosso site e reivindicares a recompensa|" +
            "Clique no link para visitar o nosso site e reivindicar sua recompensa|" +
            "Haz clic en el link para visitar nuestra web y recoger tu recompensa|" +
            "点击链接访问我们的网站并领取奖励|" +
            "點擊該網址來進入我們的網站並領取獎勵|" +
            "Klik de link om onze website te bezoeken, en je beloning te verkrijgen|" +
            "Cliquez sur le lien pour visiter notre site et réclamer votre récompense|" +
            "Klicke den Link, um unsere Webseite zu besuchen und deine Belohnung abzuholen|" +
            "Clicca il link per visitare il sito e riscattare la tua ricompensa|" +
            "リンクをクリックしてウェブサイトにアクセスし、報酬を獲得してください|" +
            "저희의 웹 사이트에 방문하고 보상을 수령하려면 링크를 클릭하세요|" +
            "Kliknij link, aby odwiedzić naszą stronę internetową i odebrać swoją nagrodę|" +
            "Нажмите на ссылку, чтобы перейти на наш сайт и забрать свою награду)" +
            ": (?:https?://rewards\\.hypixel\\.net/claim-reward/([a-zA-Z0-9]{0,12}))\\n$");

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {

        if(Quickplay.INSTANCE.onHypixel && Quickplay.INSTANCE.settings.ingameDailyReward && Quickplay.INSTANCE.isPremiumClient) {
            // Get & verify link from chat
            final Matcher matcher = pattern.matcher(event.message.getUnformattedText());
            if (matcher.find()) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    new DailyRewardInitiator(matcher.group(1));

                    // Send analytical data
                    if(Quickplay.INSTANCE.ga != null) {
                        try {
                            Quickplay.INSTANCE.ga.createEvent("Daily Reward", "Open Daily Reward")
                                    .setEventLabel(event.message.getFormattedText())
                                    .send();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
