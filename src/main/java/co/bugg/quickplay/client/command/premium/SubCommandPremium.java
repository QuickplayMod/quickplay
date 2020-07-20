package co.bugg.quickplay.client.command.premium;

import co.bugg.quickplay.client.command.ACommand;
import co.bugg.quickplay.client.command.ASubCommand;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sub command for Quickplay Premium
 */
public class SubCommandPremium extends ASubCommand {

    /**
     * List of Quickplay Premium commands
     */
    public List<IPremiumCommand> premiumCommands = new ArrayList<>();

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandPremium(ACommand parent) {
        super(
                parent,
                "premium",
                I18n.format("quickplay.commands.quickplay.premium.help"),
                "",
                true,
                true,
                91
        );

        premiumCommands.add(new PremiumCommandHelp(this));
        premiumCommands.add(new PremiumCommandAbout());
    }

    @Override
    public void run(String[] args) {
        if(premiumCommands.size() > 0) {
            if (args.length > 0) {
                // Look for the command executed by the user
                final List<IPremiumCommand> filteredList = premiumCommands
                        .stream()
                        .filter(cmd -> cmd.getName().equals(args[0]))
                        .collect(Collectors.toList());

                if(filteredList.size() > 0) {
                    filteredList.get(0).run(args);
                } else {
                    premiumCommands.get(0).run(args); // Run help command
                }
            } else {
                premiumCommands.get(0).run(args); // Run help command
            }
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        final ArrayList<String> list = new ArrayList<>();
        if(args.length == 1) {
            // Add all premium commands that begin with what's already typed out
            list.addAll(premiumCommands
                    .stream()
                    .filter(cmd -> cmd.getName().startsWith(args[0]))
                    .map(IPremiumCommand::getName)
                    .collect(Collectors.toList()));
        } else {
            // Find the Premium command which equals the currently already typed in command - Should be one item.
            final List<IPremiumCommand> filteredList = premiumCommands
                    .stream()
                    .filter(cmd -> cmd.getName().equals(args[0]))
                    .collect(Collectors.toList());
            if(filteredList.size() > 0) { // If found, get that command's tab completions.
                list.addAll(filteredList.get(0).getTabCompletions(args));
            }
        }
        return list;
    }
}
