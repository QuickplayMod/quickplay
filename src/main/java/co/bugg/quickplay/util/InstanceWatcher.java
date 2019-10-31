package co.bugg.quickplay.util;

import cc.hyperium.Hyperium;
import cc.hyperium.event.InvokeEvent;
import cc.hyperium.event.client.TickEvent;
import cc.hyperium.event.world.WorldChangeEvent;
import co.bugg.quickplay.Quickplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * When online Hypixel, enabled instances of
 * this class will watch for what instance the
 * client is on by occasionally executing
 * /whereami
 */
public class InstanceWatcher {
    /**
     * List of all instances in this game session
     * Index 0 is the latest
     */
    public List<String> instanceHistory = new ArrayList<>();
    /**
     * Whether the instance is running & registered
     * with the event handler
     */
    public boolean started = false;
    /**
     * How often in seconds /whereami should be executed
     */
    public int whereamiFrequency;

    public InstanceWatcher(int frequency) {
        whereamiFrequency = frequency;
    }

    public int tick;

    @InvokeEvent
    public void onTick(TickEvent event) {
        if (started && tick++ > whereamiFrequency * 20) {
            tick = 0;
            updateInstance();
        }
    }

    @InvokeEvent
    public void onWorldChange(WorldChangeEvent event) {
        // Run twice, just in case first one doesn't trigger
        new TickDelay(this::updateInstance, 15);
        new TickDelay(this::updateInstance, 60);
    }

    /**
     * Start the event handler tick loop & listen for chat messages
     *
     * @return this
     */
    public InstanceWatcher start() {
        Quickplay.INSTANCE.registerEventHandler(this);
        started = true;
        updateInstance();
        return this;
    }

    /**
     * Stop the event handler
     *
     * @return this
     */
    public InstanceWatcher stop() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
        started = false;
        return this;
    }

    /**
     * Determine where the client is currently at and update
     *
     * @return this
     */
    public InstanceWatcher updateInstance() {
        if (Hyperium.INSTANCE.getHandlers().getHypixelDetector().isHypixel() && Quickplay.INSTANCE.enabled) {
            final String server = Hyperium.INSTANCE.getHandlers().getLocationHandler().getLocation();

            // Automatic lobby 1 swapper
            if (Quickplay.INSTANCE.settings.lobbyOneSwap) {
                // Swap if this is true by the end of this if statement
                boolean swapToLobbyOne = true;
                // Don't swap if we aren't in a lobby or we don't know where we are
                if (server == null || !server.contains("lobby"))
                    swapToLobbyOne = false;
                    // If we have been in another server before this one
                else if (instanceHistory.size() > 0) {
                    // Get what server/lobby type this is
                    final String serverType = server.replaceAll("\\d", "");
                    // Get what server/lobby type the previous server is
                    final String previousServerType = instanceHistory.get(0).replaceAll("\\d", "");
                    // Swap if they aren't the same
                    swapToLobbyOne = !serverType.equals(previousServerType);
                }
                // Swap if: you're in a lobby & you just joined the server to a lobby or you just left an instance that was not the same type of lobby as this
                if (swapToLobbyOne)
                    Quickplay.INSTANCE.chatBuffer.push("/swaplobby 1");

            }

            if (server != null && (instanceHistory.size() <= 0 || !instanceHistory.get(0).equals(server))) {
                instanceHistory.add(0, server);

                // Send analytical data to Google
                if (Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                    Quickplay.INSTANCE.threadPool.submit(() -> {
                        try {
                            Quickplay.INSTANCE.ga.createEvent("Instance", "Instance Changed")
                                    .setEventLabel(server)
                                    .send();
                        } catch (IOException e) {
                            Hyperium.LOGGER.error(e.getMessage(), e);
                        }
                    });
                }
            }
        }
        return this;
    }

    /**
     * Get the latest instance if possible
     *
     * @return The instance
     */
    public String getCurrentServer() {
        return instanceHistory.size() > 0 ? instanceHistory.get(0) : null;
    }

}
