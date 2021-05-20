package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.GsonPostProcessorFactory;

import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * Quickplay GUI Color system
 */
public class QuickplayColor implements Serializable, GsonPostProcessorFactory.PostProcessor {
    /**
     * The Java color containing the RGB values
     */
    protected Color color;
    /**
     * The speed of this color's chroma
     * Chroma value changes every 20ms, but this is a float that contains how much
     * the hue changes every frame 20ms frame.
     * This is a value between 0 and 1.
     */
    protected float chromaSpeed;
    /**
     * The future thread that calculate's this color's chroma values
     */
    protected transient Future chromaFuture;

    /**
     * Constructor
     */
    public QuickplayColor() {
        this(1.0f, 1.0f, 1.0f, 0);
    }

    /**
     * Constructor
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     */
    public QuickplayColor(float r, float g, float b) {
        this(r, g, b, 0);
    }

    /**
     * Constructor
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @param chromaSpeed Amount at which chroma value changes at each interval
     */
    public QuickplayColor(float r, float g, float b, float chromaSpeed) {
        this.color = new Color(r, g, b);
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    /**
     * Start a thread initializing the chroma animation
     * Cancel any old threads if one exists
     */
    protected synchronized void startChromaThread() {
        if(this.chromaFuture != null) this.chromaFuture.cancel(true);

        this.chromaFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            while(getChromaSpeed() != 0) {
                float[] hsb = new float[3];
                Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), hsb);
                this.color = new Color(Color.HSBtoRGB((hsb[0] += getChromaSpeed()), hsb[1], hsb[2]));

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    /**
     * Set the speed at which chroma goes
     * @param chromaSpeed Amount the hue changes every chroma frame
     */
    public void setChromaSpeed(float chromaSpeed) {
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    /**
     * Set the Java color for this Quickplay Color
     * @param color Color object
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the speed at which chroma is changing
     * @return The speed at which chroma is changing
     */
    public float getChromaSpeed() {
        return this.chromaSpeed;
    }

    /**
     * Get this instance's Java color
     * @return Java color
     */
    public Color getColor() {
        return color;
    }

    @Override
    public void postDeserializationProcess() {
        startChromaThread();
    }

}
