package co.bugg.quickplay.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom translations for Quickplay
 * Translations are sent over the socket connection, with the lang file used as a backup.
 */
public class ConfigRegexes extends AConfiguration implements Serializable {

    private final Map<String, String> regexes = new HashMap<>();

    /**
     * Constructor
     */
    public ConfigRegexes() {
        super("regex.json");
        // Default value for daily reward regex. Will be replaced by the web server if it's online & available.
        this.regexes.put("dailyReward", "^\\n(?:" +
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
                ": (?:https?://rewards\\.hypixel\\.net/claim-reward/([a-zA-Z0-9]{0,12}))\\n$");

        // Default value for compass title regex. Will be replaced by the web server if it's online & available.
        this.regexes.put("compassTitle", "^Game Menu$");
    }

    /**
     * Get the regular expression for the specified key
     * @param key Key of the regular expression to get
     * @return regular expression corresponding to the provided key, if it exists. Otherwise, null.
     */
    public String get(final String key) {
        return this.regexes.get(key);
    }

    /**
     * Set a regular expression key's value
     * @param key The key of the regular expression key to set
     * @param val The regular expression string to set it to
     */
    public void set(final String key, final String val) {
        this.regexes.put(key, val);
    }

    public void remove(final String key) {
        this.regexes.remove(key);
    }
}
