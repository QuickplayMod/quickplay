package co.bugg.quickplay;

public class Button {
    public final String key;
    public final String[] availableOn;
    public final String[] actionKeys;
    public final String imageURL;
    public final String translationKey;
    public final boolean adminOnly;

    public Button(final String key, final String[] availableOn, final String[] actionKeys, final String imageURL,
                  final String translationKey, final boolean adminOnly) {
        this.key = key;
        this.availableOn = availableOn;
        this.actionKeys = actionKeys;
        this.imageURL = imageURL;
        this.translationKey = translationKey;
        this.adminOnly = adminOnly;
    }


}
