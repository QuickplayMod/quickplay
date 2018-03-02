package co.bugg.quickplay.client.command.premium;

import java.util.List;

public interface IPremiumCommand {



    String getName();

    String getUsage();

    String getHelpText();

    void run(final String[] args);

    List<String> getTabCompletions(final String[] args);
}
