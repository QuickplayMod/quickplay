package co.bugg.quickplay;

public class Button {
    public final String key;
    public final String[] availableOn;
    public final String protocol;
    public final String[] actionKeys;
    public final String imageURL;
    public final String translationKey;

    public Button(final String key, final String[] availableOn, final String protocol, final String[] actionKeys,
           final String imageURL, final String translationKey) {
        this.key = key;
        this.availableOn = availableOn;
        this.protocol = protocol;
        this.actionKeys = actionKeys;
        this.imageURL = imageURL;
        this.translationKey = translationKey;
    }


}
