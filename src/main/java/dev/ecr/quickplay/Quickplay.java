package dev.ecr.quickplay;

import dev.ecr.quickplay.commands.QuickplayCommand;
import dev.ecr.quickplay.util.DataManager;
import dev.ecr.quickplay.util.ImageManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(modid = QuickplayConstants.MOD_ID, useMetadata=true)
public class Quickplay {
    public static final ImageManager imageManager = new ImageManager();
    public static final ExecutorService executor = Executors.newCachedThreadPool();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new QuickplayCommand());
        executor.submit(() -> {
            imageManager.storeImage("https://bugg.co/quickplay/images/games/platform-pc-256.png");
        });
        new DataManager();
    }
}
