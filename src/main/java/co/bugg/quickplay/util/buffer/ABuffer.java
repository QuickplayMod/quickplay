package co.bugg.quickplay.util.buffer;

import co.bugg.quickplay.Quickplay;

import java.util.ArrayList;

/**
 * Abstract buffer element used for... anything buffer-related.
 * In Quickplay this is used for preparing chat messages.
 */
public abstract class ABuffer implements Runnable {
    /**
     * Buffer of all the objects
     */
    private ArrayList<Object> buffer;
    /**
     * The time in milliseconds between {@link #run()} calls
     */
    private int sleepTime;
    /**
     * Whether the buffer is currently running
     */
    private boolean started;

    /**
     * Constructor
     *
     * @param sleepTime Time in milliseconds between {@link #run()} calls. See {@link #sleepTime}
     */
    ABuffer(int sleepTime) {
        this.sleepTime = sleepTime;
        this.buffer = new ArrayList<>();
    }

    /**
     * Peek at the next buffer item without taking it out of the buffer
     *
     * @return The next buffer item
     */
    private Object peek() {
        return size() > 0 ? buffer.get(0) : null;
    }

    /**
     * Pull the next buffer item out of the buffer
     *
     * @return The next buffer item
     */
    Object pull() {
        Object returnValue = peek();
        if (returnValue != null)
            buffer.remove(0);

        return returnValue;
    }

    /**
     * Push a value to the buffer
     *
     * @param pushedValue Value to add to the buffer
     * @return this
     */
    public ABuffer push(Object pushedValue) {
        buffer.add(pushedValue);
        return this;
    }

    /**
     * Empty the buffer of all values
     *
     */
    void clear() {
        buffer.clear();
    }

    /**
     * Get the size of the buffer
     *
     * @return Number of objects in the buffer
     */
    public int size() {
        return buffer.size();
    }

    /**
     * Start looping {@link #run()}
     *
     * @return this
     */
    public ABuffer start() {
        this.started = true;
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted() && started) {
                try {
                    run();
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    stop();
                    Thread.currentThread().interrupt();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }
        });

        return this;
    }

    /**
     * Stop looping {@link #run()}
     *
     * @return this
     */
    public ABuffer stop() {
        started = false;

        return this;
    }
}
