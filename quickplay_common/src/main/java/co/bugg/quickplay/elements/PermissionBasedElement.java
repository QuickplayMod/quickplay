package co.bugg.quickplay.elements;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.config.ConfigSettings;
import co.bugg.quickplay.util.Location;
import co.bugg.quickplay.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PermissionBasedElement extends Element {
    /**
     * Whether this Element is visible at all, regardless of any other checks.
     */
    public final boolean visible;
    /**
     * Whether this Element is only visible to Quickplay Admins or not. Still must pass the other checks, even if
     * this is true.
     */
    public final boolean adminOnly;
    /**
     * List of server IDs that this Element is available on. If Quickplay#currentServers contains none of these,
     * then this Element's permission checks should fail.
     */
    public final String[] availableOn;
    /**
     * Regex data for Hypixel location fetched from /locraw command. All of the regexes in this Location object must
     * match the user's current Hypixel location, unless the regex is null.
     */
    public final Location hypixelLocrawRegex;
    /**
     * Regex data for Hypixel staff rank, fetched from the Hypixel API by the remote server. This cannot be checked
     * if the user has enabled anonymous mode. If not null, must match the player's rank fetched by the Quickplay backend.
     * If the player's rank is null, it always passes, as we can't confirm the user's rank.
     */
    public final String hypixelRankRegex;
    /**
     * Regex data for Hypixel package rank, fetched from the Hypixel API by the remote server. This cannot be checked
     * if the user has enabled anonymous mode. If not null, must match the player's rank fetched by the Quickplay backend.
     * If the player's rank is null, it always passes, as we can't confirm the user's rank.
     */
    public final String hypixelPackageRankRegex;
    /**
     * If this is true, this Element will not be available unless the player is part of the Hypixel Build team, as
     * indicated by the API. This cannot be checked if the user is in anonymous mode, and it's assumed that the user isn't
     * Hypixel Build Team. Hypixel Build Team status in the API may be deprecated.
     */
    public final boolean hypixelBuildTeamOnly;
    /**
     * If this is true, this Element will not be available unless the player is a Hypixel Build Team Admin, as
     * indicated by the API. This cannot be checked if the user is in anonymous mode, and it's assumed that the user isn't
     * Hypixel Build Team. Hypixel Build Team status in the API may be deprecated.
     */
    public final boolean hypixelBuildTeamAdminOnly;
    /**
     * Map of settings keys to RegularExpression keys. If both the setting and the RegularExpression exist, the
     * setting is converted to a String if it isn't one already and is compared against the RegularExpression.
     */
    public final Map<String, String> settingsRegexes;

    public PermissionBasedElement(String key, int elementType, final String[] availableOn, final boolean visible,
                                  final boolean adminOnly, final Location hypixelLocrawRegex,
                                  final String hypixelRankRegex, final String hypixelPackageRankRegex,
                                  final boolean hypixelBuildTeamOnly, final boolean hypixelBuildTeamAdminOnly,
                                  final Map<String, String> settingsRegexes) {
        super(key, elementType);
        this.visible = visible;
        this.adminOnly = adminOnly;
        this.availableOn = availableOn;
        this.hypixelLocrawRegex = hypixelLocrawRegex;
        this.hypixelRankRegex = hypixelRankRegex;
        this.hypixelPackageRankRegex = hypixelPackageRankRegex;
        this.hypixelBuildTeamOnly = hypixelBuildTeamOnly;
        this.hypixelBuildTeamAdminOnly = hypixelBuildTeamAdminOnly;
        this.settingsRegexes = settingsRegexes;
    }



    /**
     * Verify that this button passes specifically checks against the user's rank, and can be displayed to users with
     * the current rank. Doesn't check Hypixel rank requirements if the user isn't currently on Hypixel.
     * @return true if the users rank passes all the checks, false otherwise.
     */
    public boolean passesRankChecks() {
        // admin-only actions require admin permission.
        if(this.adminOnly && !Quickplay.INSTANCE.isAdminClient) {
            return false;
        }
        if(Quickplay.INSTANCE.isOnHypixel()) {
            // Hypixel build team-only requires that the user is a build team member.
            if(this.hypixelBuildTeamOnly && !Quickplay.INSTANCE.isHypixelBuildTeamMember) {
                return false;
            }
            // Hypixel build team admin-only requires that the user is a build team admin.
            if(this.hypixelBuildTeamAdminOnly && !Quickplay.INSTANCE.isHypixelBuildTeamAdmin) {
                return false;
            }

            // If there is a regular expression against Hypixel rank (and the user's rank is known), make sure it matches.
            if(this.hypixelRankRegex != null && Quickplay.INSTANCE.hypixelRank != null &&
                    !Pattern.compile(this.hypixelRankRegex).matcher(Quickplay.INSTANCE.hypixelRank).find()) {
                return false;
            }
            // If there is a regular expression against Hypixel package rank (and the user's package rank is known),
            // make sure it matches.
            return this.hypixelPackageRankRegex == null || Quickplay.INSTANCE.hypixelPackageRank == null ||
                    Pattern.compile(this.hypixelPackageRankRegex).matcher(Quickplay.INSTANCE.hypixelPackageRank).find();
        }
        return true;
    }

    /**
     * Verify that this button passes checks for what Minecraft server the user is currently on.
     * @return true if this button is available on the user's current server, false otherwise.
     */
    public boolean passesServerCheck() {
        // If this is only available on some servers, but the client isn't on any known servers, this never passes.
        if(Quickplay.INSTANCE.currentServers == null && this.availableOn.length > 0) {
            return false;
        }
        // Actions must be available on the current server. No availableOn values = available on all servers
        if(this.availableOn != null && this.availableOn.length > 0) {
            synchronized (Quickplay.INSTANCE.elementController.lock) {
                if (Arrays.stream(this.availableOn).noneMatch(str -> Quickplay.INSTANCE.currentServers.contains(str))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verify that this Button passes the checks for what Quickplay settings the user currently has.
     * @return True if this Button is available with the user's current settings, false otherwise.
     */
    public boolean passesSettingsChecks() {
        if(this.settingsRegexes == null || this.settingsRegexes.size() == 0) {
            return true;
        }

        for(final Map.Entry<String, String> entry : this.settingsRegexes.entrySet()) {
            // Checks always pass if entry key or entry value is null
            if(entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            // Checks always pass if regex provided isn't found
            if(Quickplay.INSTANCE.elementController == null || Quickplay.INSTANCE.elementController.regularExpressionMap == null ||
                    Quickplay.INSTANCE.elementController.regularExpressionMap.get(entry.getValue()) == null) {
                Quickplay.LOGGER.warning(String.format("Regex with name \"%s\" wasn't found!", entry.getValue()));
                continue;
            }
            final RegularExpression regex = Quickplay.INSTANCE.elementController.regularExpressionMap.get(entry.getValue());

            // If regex value is null or empty, check always passes
            if(regex.value == null || regex.value.length() == 0) {
                Quickplay.LOGGER.warning(String.format("Regex with name \"%s\" was found, but null or empty!", entry.getValue()));
                continue;
            }
            try {
                final Pattern regexPattern = Pattern.compile(regex.value);
                final Field field = ReflectionUtil.getField(ConfigSettings.class, entry.getKey());
                final Object obj = field.get(Quickplay.INSTANCE.settings);
                final String objStr = String.valueOf(obj);
                final Matcher matcher = regexPattern.matcher(objStr);
                // If the String value of the setting fails to match the regex for any of these settings then this check fails.
                if(!matcher.find()) {
                    return false;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Quickplay.LOGGER.warning(String.format("Failed to access setting \"%s\" via reflection!", entry.getKey()));
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            } catch (PatternSyntaxException e) {
                Quickplay.LOGGER.warning(String.format("Invalid regex pattern \"%s\" for setting \"%s\"!", entry.getValue(), entry.getKey()));
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }
        }
        return true;
    }

    /**
     * Verify that this button passes all checks and can be displayed to/used by the user at the current moment.
     * @return true if all checks pass, false otherwise.
     */
    public boolean passesPermissionChecks() {
        // non-"visible" actions always fail permission checks.
        if(!this.visible) {
            return false;
        }

        if(!this.passesRankChecks()) {
            return false;
        }

        if(!this.passesSettingsChecks()) {
            return false;
        }

        // Check to make sure all the location-specific requirements on Hypixel match.
        // Only perform these checks if the user is currently on Hypixel and the regular expression object to verify
        // against is not null.
        if(this.hypixelLocrawRegex != null && Quickplay.INSTANCE.isOnHypixel() &&
                Quickplay.INSTANCE.hypixelInstanceWatcher != null &&
                Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation() != null) {
            // If there is a regular expression for the "server" field, the current "server" must match.
            if(this.hypixelLocrawRegex.server != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.server)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().server)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "map" field, the current "map" must match.
            if(this.hypixelLocrawRegex.map != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.map)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().map)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "mode" field, the current "mode" must match.
            if(this.hypixelLocrawRegex.mode != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.mode)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().mode)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "gametype" field, the current "gametype" must match.
            if(this.hypixelLocrawRegex.gametype != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.gametype)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().gametype)
                            .find()) {
                return false;
            }
        }

        return this.passesServerCheck();
    }
}
