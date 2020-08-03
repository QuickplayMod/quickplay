package co.bugg.quickplay;

public class Screen {

    public final String key;
    public final String[] availableOn;
    public final String protocol;
    public final String[] buttonKeys;
    public final ScreenType screenType;
    public final String translationKey;
    public final String imageURL;
    public final String[] backButtonActions;

    public Screen(final String key, final ScreenType screenType, final String[] availableOn, final String protocol,
                  final String[] buttonKeys, final String[] backButtonActions, final String translationKey, final String imageURL) {
        this.key = key;
        this.availableOn = availableOn;
        this.protocol = protocol;
        this.buttonKeys = buttonKeys;
        this.screenType = screenType;
        this.backButtonActions = backButtonActions;
        this.translationKey = translationKey;
        this.imageURL = imageURL;
    }
}
