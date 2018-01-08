package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.GsonPostProcessorFactory;

import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.Future;

public class QuickplayColor implements Serializable, GsonPostProcessorFactory.PostProcessor {
    protected Color color;
    protected float chromaSpeed;
    protected transient Future chromaFuture;

    public QuickplayColor() {
        this(1.0f, 1.0f, 1.0f, 0);
    }

    public QuickplayColor(float r, float g, float b) {
        this(r, g, b, 0);
    }

    public QuickplayColor(float r, float g, float b, float chromaSpeed) {
        this.color = new Color(r, g, b);
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    protected synchronized void startChromaThread() {
        if(this.chromaFuture != null) this.chromaFuture.cancel(true);

        this.chromaFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            while(this.chromaSpeed != 0) {
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

    public void setChromaSpeed(float chromaSpeed) {
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getChromaSpeed() {
        return this.chromaSpeed;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void postDeserializationProcess() {
        startChromaThread();
    }

}
