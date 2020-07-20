package co.bugg.quickplay.client.gui.animations;

import co.bugg.quickplay.Quickplay;

import java.util.concurrent.Future;

/**
 * {@link Animation} running on a static timer calling {@link #updateFrame()}, as opposed to
 * calling the method manually (i.e. before drawing each frame).
 */
public class StaticAnimation extends Animation {

    /**
     * Frequency in milliseconds that the progress is updated
     * Also known as framerate
     *
     * Default 25ms, or 40 frames per second
     */
    public int updateFrequency;

    /**
     * Future containing the thread that updates the progress
     */
    public Future threadFuture;

    /**
     * Construction
     * @param length The length of this animation in milliseconds
     * @param framerate The rate at which updateFrame() is called per second
     */
    public StaticAnimation(long length, int framerate) {
        super(length);
        updateFrequency = 1000 / framerate;
    }

    @Override
    public Animation start() {
        super.start();
        if(threadFuture != null) {
            threadFuture.cancel(true);
        }
        threadFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            try {
                Thread.sleep(updateFrequency);
                while(progress < 1) {
                    updateFrame();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return this;
    }
}
