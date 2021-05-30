package co.bugg.quickplay.elements;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Location;

import java.util.Map;

public class Button extends PermissionBasedElement {
    public final String[] actionKeys;
    public final String imageURL;
    public final String translationKey;
    public final boolean visibleInPartyMode;
    public final String partyModeScopeTranslationKey;

    public Button(final String key, final String[] availableOn, final String[] actionKeys, final String imageURL,
                  final String translationKey, final boolean visible, final boolean adminOnly,
                  final Location hypixelLocrawRegex, final String hypixelRankRegex, final String hypixelPackageRankRegex,
                  final boolean hypixelBuildTeamOnly, final boolean hypixelBuildTeamAdminOnly, final boolean visibleInPartyMode,
                  final String partyModeScopeTranslationKey, final Map<String, String> settingsRegexes) {
        super(key, ElementType.BUTTON.getValue(), availableOn, visible, adminOnly, hypixelLocrawRegex, hypixelRankRegex, hypixelPackageRankRegex,
                hypixelBuildTeamOnly, hypixelBuildTeamAdminOnly, settingsRegexes);
        this.actionKeys = actionKeys;
        this.imageURL = imageURL;
        this.translationKey = translationKey;
        this.visibleInPartyMode = visibleInPartyMode;
        this.partyModeScopeTranslationKey = partyModeScopeTranslationKey;
    }

    public void run() {
        if(this.actionKeys == null) {
            Quickplay.LOGGER.warning("Action keys are null for button " + this.key);
            return;
        }
        for (String actionKey : this.actionKeys) {
            if (actionKey == null || actionKey.length() <= 0) {
                continue;
            }
            AliasedAction aliasedAction = Quickplay.INSTANCE.elementController.getAliasedAction(actionKey);
            if (aliasedAction == null) {
                Quickplay.LOGGER.warning("Aliased action " + actionKey + " is not found.");
                continue;
            }
            if (!aliasedAction.passesPermissionChecks()) {
                Quickplay.LOGGER.warning("Aliased action " + actionKey + " does not pass permission checks.");
                continue;
            }
            if (aliasedAction.action != null) {
                aliasedAction.action.run();
            }
        }
    }

}
