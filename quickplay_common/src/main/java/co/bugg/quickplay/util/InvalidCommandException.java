package co.bugg.quickplay.util;

/**
 * When a command is executed with arguments, those arguments are parsed in attempt to find a subcommand
 * with a name matching the relevant argument. If a subcommand is not found, this exception is thrown.
 * Typically the direct parent will want to catch and handle the exception. If no ancestor command handles the
 * exception, {@link co.bugg.quickplay.client.command.BaseCommand} will send an "Invalid command!" message.
 */
public class InvalidCommandException extends Exception {
}
