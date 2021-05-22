package co.bugg.quickplay.util.buffer;

import co.bugg.quickplay.Quickplay;

import java.util.ArrayList;
import java.util.Date;

/**
 * Buffers allow for the developer to easily queue data to be pulled at a set interval.
 */
public abstract class ABuffer<T> implements Runnable {
    /**
     * Buffer of all the objects
     */
    private final ArrayList<T> buffer;
    /**
     * Time in milliseconds between {@link #run()} calls when in "burst" mode.
     * Burst mode allows for rapid calls to {@link #run()} in limited quantities.
     */
    private final int burstDelay;
    /**
     * Total number of items allowed in any given burst before normal speed must be used.
     */
    private final int burstCap;
    /**
     * Total time in milliseconds that a burst lasts. Once this time elapses, {@link #currentBurstItems}
     * and {@link #currentBurstStarted} are reset, allowing for a new burst.
     */
    private final int burstCooldown;
    /**
     * The time in milliseconds between {@link #run()} calls when not in burst mode.
     */
    private final int normalDelay;
    /**
     * Whether the buffer is currently running
     */
    private boolean started;
    /**
     * Timestamp in milliseconds of when this current burst started.
     */
    private long currentBurstStarted = -1;
    /**
     * Total number of items that have flowed through this buffer during the current burst.
     */
    private long currentBurstItems = 0;

    public ABuffer(int normalDelay) {
        this(normalDelay, normalDelay);
    }
    /**
     * Constructor
     * @param burstDelay The delay nec
     */
    public ABuffer(int burstDelay, int normalDelay) {
        this(burstDelay, 5, 5000, normalDelay);
    }
    public ABuffer(int burstDelay, int burstCap, int burstCooldown, int normalDelay) {
        this.burstDelay = burstDelay;
        this.burstCap = burstCap;
        this.burstCooldown = burstCooldown;
        this.normalDelay = normalDelay;
        this.buffer = new ArrayList<>();
    }

    /**
     * Peek at the next buffer item without taking it out of the buffer
     * @return The next buffer item
     */
    public T peek() {
        return size() > 0 ? this.buffer.get(0) : null;
    }

    /**
     * Pull the next buffer item out of the buffer
     * @return The next buffer item
     */
    public T pull() {
        T returnValue = this.peek();
        if(returnValue != null) {
            this.buffer.remove(0);
        }

        return returnValue;
    }

    /**
     * Push a value to the buffer
     * @param pushedValue Value to add to the buffer
     * @return this
     */
    public ABuffer<T> push(T pushedValue) {
        this.buffer.add(pushedValue);
        return this;
    }

    /**
     * Empty the buffer of all values
     * @return this
     */
    public ABuffer<T> clear() {
        this.buffer.clear();
        return this;
    }

    /**
     * Get the size of the buffer
     * @return Number of objects in the buffer
     */
    public int size() {
        return this.buffer.size();
    }

    /**
     * Getter for {@link #started}
     * @return {@link #started}
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Start looping {@link #run()}
     * @return this
     */
    public ABuffer<T> start() {
        this.started = true;
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted() && this.started) {
                int delay = this.normalDelay;
                long now = new Date().getTime();
                /* Check if we're able to burst. Can burst if either:
                       - "Current" burst has expired, in which case we restart the burst
                       - Current burst hasn't hit it's item cap
                       - No items are currently in the buffer
                 */
                if(this.size() <= 0) {
                    delay = this.burstDelay;
                }else if(this.currentBurstStarted < now - this.burstCooldown) {
                    this.currentBurstStarted = now;
                    this.currentBurstItems = 1;
                    delay = this.burstDelay;
                } else if(this.currentBurstItems < this.burstCap) {
                    this.currentBurstItems++;
                    delay = this.burstDelay;
                }
                try {
                    if(this.size() > 0) {
                        this.run();
                    }
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    this.stop();
                    Thread.currentThread().interrupt();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }
        });

        return this;
    }

    /**
     * Stop looping {@link #run()}
     * @return this
     */
    public ABuffer<T> stop() {
        started = false;

        return this;
    }
}
