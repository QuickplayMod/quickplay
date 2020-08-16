package co.bugg.quickplay.actions.serverbound;

/**
 * Identifier types to signal to the backend the type of identifier sent, whether it's a Google account ID or
 * a Mojang UUID.
 * @see InitializeClientAction
 */
public enum IdentifierTypes {
    GOOGLE, MOJANG
}
