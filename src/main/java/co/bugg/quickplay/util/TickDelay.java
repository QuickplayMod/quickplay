package co.bugg.quickplay.util;

import co.bugg.quickplay.Quickplay;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Class to delay code by a certain number of
 * game ticks
 * @author bugfroggy
 */
public class TickDelay {

    /**
     * Constructor
     * @param fn Code to be delayed
     * @param ticks How many ticks to delay it
     */
    public TickDelay(Runnable fn, int ticks) {
        this.fn = fn;
        this.delay = ticks;

        Quickplay.INSTANCE.registerEventHandler(this);
    }

    /**
     * Default 20 ticks when unprovided
     * @param fn Code to be delayed
     */
    public TickDelay(Runnable fn) {
        this(fn, 20);
    }

    public Runnable fn;
    public int delay;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            // Delay expired
            if(delay < 1) {
                run();
                destroy();
            }
            delay--;
        }
    }

    public void run() {
        fn.run();
    }

    public void destroy() {
        Quickplay.INSTANCE.unregisterEventHandler(this);
    }
}
