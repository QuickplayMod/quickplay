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
     * @return This
     */
    public Animation start() {
        startedMillis = System.currentTimeMillis();
        progress = 0;
        started = true;
        return this;
    }

    /**
     * Refresh {@link #progress}
     * @return This
     */
    public synchronized Animation updateFrame() {
        // If started
        if(startedMillis >= 0) {
            final long now = System.currentTimeMillis();

            if(now - startedMillis < 0) {
                progress = 0;
            } else if(startedMillis + length > now) {
                progress = ((float) (now - startedMillis)) / (float) length;
            } else progress = 1;

        } else throw new IllegalStateException("This animation has not been started yet. You must call start() first.");
        return this;
    }
}
