package dev.ecr.quickplay.util;

import gg.essential.elementa.components.UIImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ImageManager {

    private final Map<String, UIImage> store = new HashMap<>();

    private BufferedImage fetchImageFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

            // Using ImageIO to read the image asynchronously
            return ImageIO.read(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null or handle error appropriately
        }
    }

    public void storeImage(String urlString) {
        BufferedImage img = this.fetchImageFromUrl(urlString);
        this.store.put(urlString, new UIImage(CompletableFuture.completedFuture(img)));
    }

    public UIImage getImage(String urlString) {
        return this.store.get(urlString);
    }
}
