package co.bugg.quickplay.client.gui.animations;

/**
 * Basic class capable of timing GUI animations and their progress
 */
public class Animation {

    /**
     * The length of this animation in milliseconds
     */
    public long length = 500;
    /**
     * Percentage-based progress on this animation, between 0 and 1
     * Numbers lower than 0 are treated as 0 and numbers higher than 1 are treated as 1
     */
    public double progress = 0;
    /**
     * Whether this animation has been started yet
     */
    public boolean started = false;
    /**
     * The timestamp that this animation started at
     * -1 if it hasn't started or has been stopped
     */
    private long startedMillis = -1;

    /**
     * Constructor
     */
    public Animation() { }

    /**
     * Constructor
     *
     * @param length The length of this animation in milliseconds
     */
    public Animation(long length) {
        this.length = length;
    }

    /**
     * Start this animation, starting from 0
     * Thread-blocking until {@link #updateFrame()} is called and {@link #progress} >= 1 post-update
     * @return This
     */
    public Animation start() {
        startedMillis = System.currentTimeMillis();
        progress = 0;
        started = true;

        synchronized (this) {
            while (progress < 1) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return this;
    }

    /**
     * Stop this animation from progressing any further
     * <code>notify()</code> is called
     * @return This
     */
    public Animation stop() {
        startedMillis = -1;
        started = false;
        synchronized (this) {
            notify();
        }
        return this;
    }

    /**
     * Refresh {@link #progress}
     * @return This
     */
    public synchronized Animation updateFrame() {
        // If started
        if(started) {
            final long now = System.currentTimeMillis();

            if(now - startedMillis < 0) {
                progress = 0;
            } else if(startedMillis + length > now) {
                progress = ((float) (now - startedMillis)) / (float) length;
            } else progress = 1;

        } else throw new IllegalStateException("This animation has not been started yet. You must call start() first.");

        if(progress >= 1) {
            notify();
        }

        return this;
    }
}
