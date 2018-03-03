package co.bugg.quickplay.client.render;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Renders all Quickplay Glyphs when registered to the event bus
 * @see Quickplay#glyphs
 */
public class GlyphRenderer {

    /**
     * Vertical offset from the player's head
     */
    public static final double offset = 1.0;
    /**
     * How far away glyphs should render
     */
    public static final int drawDistance = 64;
    /**
     * Regex pattern for determining if the client is currently in a game or not
     */
    public static final Pattern gameServerPattern = Pattern.compile("^(?:mini|mega)[0-9]{1,3}[A-Z]$");

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre e) {
        final String currentServer = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
        // Don't render at all if F1 is hit or if the client is in a game (or unknown location)
        if(!Minecraft.getMinecraft().gameSettings.hideGUI && currentServer != null && !gameServerPattern.matcher(currentServer).matches()) {
            final EntityPlayer player = e.entityPlayer;
            final EntityPlayer me = Minecraft.getMinecraft().thePlayer;

            // If both players aren't null, player is visible, and player isn't dead
            if (player != null && me != null && !player.isInvisible() && !player.isDead && me.canEntityBeSeen(player) && me.getDistanceSqToEntity(player) < drawDistance * drawDistance) {

                if(Quickplay.INSTANCE.glyphs.stream().anyMatch(glyph -> glyph.userUUID.toString().equals(player.getGameProfile().getId().toString()))) {
                    renderGlyph(e.renderer, Quickplay.INSTANCE.glyphs.stream().filter(glyph -> glyph.userUUID.equals(player.getGameProfile().getId())).collect(Collectors.toList()).get(0), e.entityPlayer, e.x, e.y + offset + player.height, e.z);
                }
            }

        }
    }

    /**
     * Render a glyph
     * @param renderer Renderer to use
     * @param glyph Glyph to render
     * @param player Player to render it over
     * @param x x position
     * @param y y position
     * @param z z position
     */
    public void renderGlyph(RendererLivingEntity renderer, PlayerGlyph glyph, EntityPlayer player, double x, double y, double z) {

        final ResourceLocation resource = new ResourceLocation(Reference.MOD_ID, "glyphs/" + Hashing.md5().hashString(glyph.resource.toString(), Charset.forName("UTF-8")).toString() + ".png");
        if(Quickplay.INSTANCE.resourcePack.resourceExists(resource)) {
            float scale = (float) (glyph.height * 0.0015);

            // Apply GL properties
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            // Rotate rendering axes
            GlStateManager.rotate(-renderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

            // Calculate x-axis rotation
            int xRotationMultiplier = 1;
            // no x-rotation if in inventory & rendering self
            if (player == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().currentScreen instanceof GuiInventory)
                xRotationMultiplier = 0;
                // Flip x rotation if in front-facing 3rd person
            else if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
                xRotationMultiplier = -1;
            GlStateManager.rotate(renderer.getRenderManager().playerViewX * xRotationMultiplier, 1.0F, 0.0F, 0.0F);

            // Scale
            GlStateManager.scale(-scale, -scale, scale);

            // Draw texture
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            renderer.bindTexture(resource);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double) (-16), (double) (-16), 0.0D).tex(0, 0).endVertex();
            worldrenderer.pos((double) (-16), (double) (16), 0.0D).tex(0, 1).endVertex();
            worldrenderer.pos((double) (16), (double) (16), 0.0D).tex(1, 1).endVertex();
            worldrenderer.pos((double) (16), (double) (-16), 0.0D).tex(1, 0).endVertex();
            tessellator.draw();

            // Remove GL properties
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else if(!glyph.downloadAttempted) {
            Quickplay.INSTANCE.threadPool.submit(glyph::download);
        }
    }
}
