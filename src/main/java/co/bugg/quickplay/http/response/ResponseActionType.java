package co.bugg.quickplay.http.response;

/**
 * Possible action responses from mod endpoints
 * @see ResponseAction
 */
public enum ResponseActionType {
    SEND_MESSAGE,
    RELOAD_GAMES,
    DISABLE_MOD,
    ENABLE_MOD,
    SYSTEM_OUT,
    RESET_CONFIG,
    REFRESH_CACHE,
    START_PING
}
