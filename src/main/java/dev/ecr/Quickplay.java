package dev.ecr;

import dev.ecr.commands.QuickplayCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = QuickplayConstants.MOD_ID, useMetadata=true)
public class Quickplay {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new QuickplayCommand());
    }
}
