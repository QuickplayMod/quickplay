package co.bugg.quickplay;

import co.bugg.quickplay.wrappers.ResourceLocationWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.8, 1.8.9]"
)
public class QuickplayMod {

    /**
     * Quickplay's resource pack
     */
    public IResourcePack resourcePack;
    /**
     * A list of all registered commands
     */
    public final List<ICommand> commands = new ArrayList<>();


    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Quickplay.INSTANCE.mod = this;
        Quickplay.INSTANCE.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Quickplay.INSTANCE.init();
    }

    /**
     * Register a specific object as an event handler
     * @param handler Object to register
     */
    public void registerEventHandler(Object handler) {
        MinecraftForge.EVENT_BUS.register(handler);
    }

    /**
     * Unregister a specific object as an event handler
     * @param handler Object to unregister
     */
    public void unregisterEventHandler(Object handler) {
        MinecraftForge.EVENT_BUS.unregister(handler);
    }

    /**
     * Reload the provided resourceLocation with the provided file
     * @param file The file of the newly changed resource
     * @param resourceLocation The resourceLocation to change/set
     */
    public void reloadResource(File file, ResourceLocationWrapper resourceLocation) {
        if (file != null && file.exists()) {

            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            texturemanager.deleteTexture(resourceLocation.get());
            ITextureObject object = new ThreadDownloadImageData(file, null, resourceLocation.get(), null);
            texturemanager.loadTexture(resourceLocation.get(), object);
        }
    }

    public void registerCommands() {
        this.commands.forEach(ClientCommandHandler.instance::registerCommand);
    }
}
