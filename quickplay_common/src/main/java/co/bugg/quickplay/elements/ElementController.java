package co.bugg.quickplay.elements;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.config.AssetFactory;
import com.google.common.io.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

public class ElementController implements Serializable {

    public static final transient String defaultLang = "en_US";

    /**
     * Map of screens, mapping their key to the screen object.
     */
    public Map<String, Screen> screenMap = new HashMap<>();
    /**
     * Map of buttons, mapping their key to the button object.
     */
    public Map<String, Button> buttonMap = new HashMap<>();
    /**
     * Map of aliased actions, mapping their key to the aliased action object.
     */
    public Map<String, AliasedAction> aliasedActionMap = new HashMap<>();
    /**
     * Map of aliased actions, mapping their key to the aliased action object.
     */
    public Map<String, Translation> translationMap = new HashMap<>();
    /**
     * Map of aliased actions, mapping their key to the aliased action object.
     */
    public Map<String, RegularExpression> regularExpressionMap = new HashMap<>();

    public ElementController() {
        // Default value for daily reward regex. Will be replaced by the web server if it's online & available.
        this.regularExpressionMap.put("dailyReward", new RegularExpression("dailyReward", "^\\n(?:" +
                "Click the link to visit our website and claim your reward|" +
                "Clica no link para visitares o nosso site e reivindicares a recompensa|" +
                "Clique no link para visitar o nosso site e reivindicar sua recompensa|" +
                "Haz clic en el link para visitar nuestra web y recoger tu recompensa|" +
                "点击链接访问我们的网站并领取奖励|" +
                "點擊該網址來進入我們的網站並領取獎勵|" +
                "Klik de link om onze website te bezoeken, en je beloning te verkrijgen|" +
                "Cliquez sur le lien pour visiter notre site et réclamer votre récompense|" +
                "Klicke den Link, um unsere Webseite zu besuchen und deine Belohnung abzuholen|" +
                "Clicca il link per visitare il sito e riscattare la tua ricompensa|" +
                "リンクをクリックしてウェブサイトにアクセスし、報酬を獲得してください|" +
                "저희의 웹 사이트에 방문하고 보상을 수령하려면 링크를 클릭하세요|" +
                "Kliknij link, aby odwiedzić naszą stronę internetową i odebrać swoją nagrodę|" +
                "Нажмите на ссылку, чтобы перейти на наш сайт и забрать свою награду)" +
                ": (?:https?://rewards\\.hypixel\\.net/claim-reward/([a-zA-Z0-9]{0,12}))\\n$"));

        // Default value for compass title regex. Will be replaced by the web server if it's online & available.
        this.regularExpressionMap.put("compassTitle", new RegularExpression("compassTitle", "^Game Menu$"));
    }

    public void putElement(Element e) {
        if(e instanceof Screen) {
            this.screenMap.put(e.key, (Screen) e);
        } else if(e instanceof Button) {
            this.buttonMap.put(e.key, (Button) e);
        } else if(e instanceof AliasedAction) {
            this.aliasedActionMap.put(e.key, (AliasedAction) e);
        } else if(e instanceof Translation) {
            this.translationMap.put(e.key, (Translation) e);
        } else if(e instanceof RegularExpression) {
            this.regularExpressionMap.put(e.key, (RegularExpression) e);
        }
        saveCache();
    }

    public void saveCache() {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;

        try {
            objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();

            File cacheFile = new File(AssetFactory.elementsCacheFile);
            Files.write(Base64.getEncoder().encode(byteOutputStream.toByteArray()), cacheFile);
        } catch (IOException e) {
            System.out.println("Error while attempting to save element controller cache!");
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        } finally {
            try {
                byteOutputStream.close();
                if(objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ElementController loadCache() throws FileNotFoundException {
        File cacheFile = new File(AssetFactory.elementsCacheFile);

        if(!cacheFile.exists()) {
            throw new FileNotFoundException("Configuration file \"" + AssetFactory.elementsCacheFile + "\" not found.");
        }
        try {
            byte[] bytes = Files.toByteArray(cacheFile);
            bytes = Base64.getDecoder().decode(bytes);

            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(byteInputStream);
                Object obj = objectInputStream.readObject();

                if(!(obj instanceof ElementController)) {
                    throw new InvalidClassException("Expected class ElementController, found: " + obj.getClass().getName());
                }

                return (ElementController) obj;
            } finally {
                byteInputStream.close();
                if(objectInputStream != null) {
                    objectInputStream.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ElementController();
    }

    public Screen getScreen(String key) {
        return this.screenMap.get(key);
    }

    public Button getButton(String key) {
        return this.buttonMap.get(key);
    }

    public AliasedAction getAliasedAction(String key) {
        return this.aliasedActionMap.get(key);
    }

    public Translation getTranslation(String key) {
        return this.translationMap.get(key);
    }

    public RegularExpression getRegularExpression(String key) {
        return this.regularExpressionMap.get(key);
    }

    public void removeScreen(String key) {
        if(this.screenMap.remove(key) != null) {
            this.saveCache();
        }
    }

    public void removeButton(String key) {
        if(this.buttonMap.remove(key) != null) {
            this.saveCache();
        }
    }

    public void removeAliasedAction(String key) {
        if(this.aliasedActionMap.remove(key) != null) {
            this.saveCache();
        }
    }

    public void removeTranslation(String key) {
        if(this.translationMap.remove(key) != null) {
            this.saveCache();
        }
    }

    public void removeRegularExpression(String key) {
        if(this.regularExpressionMap.remove(key) != null) {
            this.saveCache();
        }
    }

    /**
     * Get the translation for the specified key
     * @param key Key of the translation to get
     * @param args Arguments provided to the translation
     * @return Formatted translation
     */
    public String translate(final String key, String... args) {
        final Translation translation = this.translationMap.get(key);

        if(translation == null) {
            return I18n.format(key, (Object[]) args);
        }

        String translatedStr = translation.getValueForLanguage(this.getCurrentLangKey().toLowerCase());
        // Swap to default language if translation is null in current language
        if(translatedStr == null) {
            translatedStr = translation.getValueForLanguage(ElementController.defaultLang.toLowerCase());
        }
        if(translatedStr == null) {
            return I18n.format(key, (Object[]) args);
        }

        try {
            return String.format(translatedStr, (Object[]) args);
        } catch (IllegalFormatException e) {
            return "Format error: " + key;
        }
    }

    /**
     * Set a translation key's value
     * @param key The key of the translation to set
     * @param lang The language to set this value in
     * @param val The translated string to set it to
     */
    public void setTranslation(final String key, final String lang, final String val) {
        Translation translation = this.translationMap.get(key);
        if(translation == null) {
            translation = new Translation(key);
            this.putElement(translation);
        }

        translation.setValueForLanguage(lang, val);
        this.saveCache();
    }

    public void removeTranslation(final String key, final String lang) {
        Translation translation = this.getTranslation(key);
        if(translation == null) {
            return;
        }

        translation.removeValueForLanguage(lang);
        if(translation.values.size() == 0) {
            this.removeTranslation(key);
        } else {
            this.saveCache();
        }
    }

    /**
     * Get the current language used in Minecraft.
     * @return The language currently used by Minecraft.
     */
    private String getCurrentLangKey() {
        return Minecraft.getMinecraft().gameSettings.language;
    }
}
