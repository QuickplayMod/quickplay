package co.bugg.quickplay.elements;

import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.util.Location;

import java.util.Map;

public class AliasedAction extends PermissionBasedElement {
    public final Action action;

    public AliasedAction(final String key, final String[] availableOn, final Action action, final boolean visible,
                         final boolean adminOnly, final Location hypixelLocrawRegex, final String hypixelRankRegex,
                         final String hypixelPackageRankRegex, final boolean hypixelBuildTeamOnly,
                         final boolean hypixelBuildTeamAdminOnly, final Map<String, String> settingsRegexes) {
        super(key, ElementType.ALIASED_ACTION.getValue(), availableOn, visible, adminOnly, hypixelLocrawRegex, hypixelRankRegex,
                hypixelPackageRankRegex, hypixelBuildTeamOnly, hypixelBuildTeamAdminOnly, settingsRegexes);
        this.action = action;
    }
}
