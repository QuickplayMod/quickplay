package co.bugg.quickplay;

import co.bugg.quickplay.util.ServerChecker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.8, 1.12.2]"
)
public class Quickplay {

    @Mod.Instance
    public static Quickplay INSTANCE = new Quickplay();

    /**
     * Whether the client is currently connected to the Hypixel network
     */
    public boolean onHypixel = false;
    /**
     * Verification method used to verify the client is online Hypixel
     */
    public ServerChecker.VerificationMethod verificationMethod;

    public ExecutorService threadPool = Executors.newCachedThreadPool();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new QuickplayEventHandler());
    }
}
