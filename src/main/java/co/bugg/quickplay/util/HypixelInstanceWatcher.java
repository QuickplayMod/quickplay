package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.serverbound.HypixelLocationChangedAction;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * When online Hypixel, enabled instances of
 * this class will watch for what instance the
 * client is on by executing /locraw whenever the
 * client changes worlds.
 */
public class HypixelInstanceWatcher {
    /**
     * List of all locations in this game session
     * Index 0 is the latest
     */
    public List<Location> instanceHistory = new ArrayList<>();
    /**
     * Whether the instance is running & registered
     * with the event handler
     */
    public boolean started = false;
    /**
     * State of the location detection; Used to determine if a LocationDetector is already created and working,
     */
    private boolean polling = false;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        detectLocation();
    }

    /**
     * Start the event handler & listen for chat messages
     * @return this
     */
    public HypixelInstanceWatcher start() {
        Quickplay.INSTANCE.registerEventHandler(this);
        this.started = true;
        this.detectLocation();
        return this;
    }

    /**
     * Stop the event handler. This does not stop any ongoing polls, only prevents new ones from being created.
     * @return this
     */
    public HypixelInstanceWatcher stop() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
        this.started = false;
        return this;
    }

    public void detectLocation() {
        if(!this.polling) {
            Location startingLocation = this.getCurrentLocation();
            this.polling = true;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int counter = 0;
                @Override
                public void run() {
                    // Stop polling if not on Hypixel or client is not enabled
                    if(!Quickplay.INSTANCE.isOnHypixel() || !Quickplay.INSTANCE.isEnabled) {
                        polling = false;
                        timer.cancel();
                        return;
                    }
                    // Stop polling after 10 rounds
                    if(counter++ >= 10) {
                        timer.cancel();
                        polling = false;
                        return;
                    }
                    new LocationDetector(location -> {
                        // Only finalize if the location has changed, and it has not changed to limbo.
                        // This could result in running /locraw more times than necessary, but this is better than
                        // having incorrect data.
                        if(location != null && !location.equals(startingLocation) &&
                                location.server != null && !location.server.equals("limbo")) {
                            timer.cancel();
                            polling = false;

                            // Null values are replaced with empty strings to avoid null pointers and having to check
                            // that things are non-null down stream.
                            if(location.map == null) {
                                location.map = "";
                            }
                            if(location.mode == null) {
                                location.mode = "";
                            }
                            if(location.gametype == null) {
                                location.gametype = "";
                            }

                            handleLobbySwap(location);
                            logLocationChange(location);
                        }
                    });
                }
            }, 0, 1500);
        }
    }

    /**
     * Determine if the client needs to swap to lobby 1 (based on their lobbyOneSwap setting), and execute
     * the command, if necessary.
     * @param newLocation The location the client just joined, used to compare to their old location and
     *                    check if it meets the criteria to require a swap to lobby 1.
     */
    public void handleLobbySwap(Location newLocation) {
        // Automatic lobby 1 swapper
        if (Quickplay.INSTANCE.settings.lobbyOneSwap) {
            // Swap if this is true by the end of this if statement
            boolean swapToLobbyOne = true;
            // Don't swap if we aren't in a lobby or we don't know where we are
            if (newLocation == null || !newLocation.server.contains("lobby")) {
                swapToLobbyOne = false;
            }
            // If we have been in another server before this one
            else if (instanceHistory.size() > 0) {
                // Get what server/lobby type this is
                final String serverType = newLocation.server.replaceAll("\\d", "");
                // Get what server/lobby type the previous server is
                final String previousServerType = instanceHistory.get(0).server.replaceAll("\\d", "");
                // Swap if they aren't the same
                swapToLobbyOne = !serverType.equals(previousServerType);
            }
            // Swap if: you're in a lobby & you just joined the server to a lobby or you just left an instance that was not the same type of lobby as this
            if (swapToLobbyOne) {
                Quickplay.INSTANCE.chatBuffer.push("/swaplobby 1");
            }

        }
    }

    /**
     * Log a new location change to analytics and to the user's instance history
     * @param location The location the user just changed to that should be logged.
     */
    public void logLocationChange(Location location) {
        if (location != null && (instanceHistory.size() <= 0 || !instanceHistory.get(0).equals(location))) {
            instanceHistory.add(0, location);
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.socket.sendAction(new HypixelLocationChangedAction(location));
                } catch (ServerUnavailableException e) {
                    e.printStackTrace();
                }
            });

            // Send analytical data to Google
            if (Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null &&
                    Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Instance", "Instance Changed")
                                .setEventLabel(location.toString())
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * Get the latest location of the client, if it exists.
     * @return The latest location of the client.
     */
    public Location getCurrentLocation() {
        if(this.instanceHistory == null || this.instanceHistory.size() <= 0) {
            return null;
        }
        return this.instanceHistory.get(0);
    }
    /**
     * Get the latest instance if possible
     * @return The instance
     */
    public String getCurrentServer() {
        final Location loc = this.getCurrentLocation();
        if(loc == null) {
            return null;
        }
        return loc.server;
    }

}
