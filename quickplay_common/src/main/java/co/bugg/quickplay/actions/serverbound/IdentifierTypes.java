package co.bugg.quickplay.actions.serverbound;

/**
 * Identifier types to signal to the backend the type of identifier sent, whether it's a Google account ID,
 * a Mojang UUID, or null (anonymous mode)
 * @see InitializeClientAction
 */
public enum IdentifierTypes {
    GOOGLE, MOJANG, ANONYMOUS
}
