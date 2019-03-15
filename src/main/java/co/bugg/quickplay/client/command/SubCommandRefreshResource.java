package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.QuickplayEventHandler;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.config.AssetFactory;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sub command to refresh Quickplay resources
 */
public class SubCommandRefreshResource extends ASubCommand {

    /**
     * Constructor
     *
     * @param parent Parent command
     */
    public SubCommandRefreshResource(ACommand parent) {
        super(
                parent,
                "refreshresource",
                "Refresh the provided Quickplay Resource.",
                "<path>",
                false,
                true,
                -10000
        );
    }

    @Override
    public void run(String[] args) {
        if (args.length >= 1)
            QuickplayEventHandler.mainThreadScheduledTasks.add(() -> {
                // Reload the resource pack
                Quickplay.INSTANCE.reloadResource(new File(AssetFactory.assetsDirectory + "/" + args[0]), new ResourceLocation(Reference.MOD_ID, args[0]));
                System.out.println("Reloaded resource " + args[0]);
            });
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
