package co.bugg.quickplay.client;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.GsonPostProcessorFactory;

import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.Future;

public class QuickplayColor implements Serializable, GsonPostProcessorFactory.PostProcessor {
    protected Color color;
    protected ChromaSpeed chromaSpeed;
    protected transient Future chromaFuture;

    public QuickplayColor() {
        this(1.0f, 1.0f, 1.0f, ChromaSpeed.OFF);
    }

    public QuickplayColor(float r, float g, float b) {
        this(r, g, b, ChromaSpeed.OFF);
    }

    public QuickplayColor(float r, float g, float b, ChromaSpeed chromaSpeed) {
        this.color = new Color(r, g, b);
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    protected synchronized void startChromaThread() {
        if(this.chromaFuture != null) this.chromaFuture.cancel(true);

        this.chromaFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
            while(this.chromaSpeed != ChromaSpeed.OFF) {
                float[] hsb = new float[3];
                Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), hsb);
                this.color = new Color(Color.HSBtoRGB((hsb[0] += ChromaSpeed.FAST.getSpeed()), hsb[1], hsb[2]));

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    public void setChromaSpeed(ChromaSpeed chromaSpeed) {
        this.chromaSpeed = chromaSpeed;
        startChromaThread();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ChromaSpeed getChromaSpeed() {
        return this.chromaSpeed;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void postDeserializationProcess() {
        startChromaThread();
    }

    public enum ChromaSpeed implements Serializable {
        OFF(0.0f),
        SLOW(0.002f),
        NORMAL(0.01f),
        FAST(0.03f);

        float speed;

        ChromaSpeed(float speed) {
            this.speed = speed;
        }

        public float getSpeed() {
            return speed;
        }
    }


}
