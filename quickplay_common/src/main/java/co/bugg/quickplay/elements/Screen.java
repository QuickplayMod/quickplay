package co.bugg.quickplay.elements;

import co.bugg.quickplay.util.Location;

import java.util.Map;

public class Screen extends PermissionBasedElement {

    public final String[] buttonKeys;
    public final ScreenType screenType;
    public final String translationKey;
    public final String imageURL;
    public final String[] backButtonActions;

    public Screen(final String key, final ScreenType screenType, final String[] availableOn, final String[] buttonKeys,
                  final String[] backButtonActions, final String translationKey, final String imageURL,
                  final boolean visible, final boolean adminOnly, final Location hypixelLocrawRegex,
                  final String hypixelRankRegex, final String hypixelPackageRankRegex,
                  final boolean hypixelBuildTeamOnly, final boolean hypixelBuildTeamAdminOnly,
                  final Map<String, String> settingsRegexes) {
        super(key, ElementType.SCREEN.getValue(), availableOn, visible, adminOnly, hypixelLocrawRegex, hypixelRankRegex,
                hypixelPackageRankRegex, hypixelBuildTeamOnly, hypixelBuildTeamAdminOnly, settingsRegexes);
        this.buttonKeys = buttonKeys;
        this.screenType = screenType;
        this.backButtonActions = backButtonActions;
        this.translationKey = translationKey;
        this.imageURL = imageURL;
    }
}
