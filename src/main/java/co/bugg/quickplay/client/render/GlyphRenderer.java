package co.bugg.quickplay.client.render;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderPlayer;
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
        if(!Minecraft.getMinecraft().gameSettings.hideGUI) {
            final EntityPlayer player = e.getEntityPlayer();
            final EntityPlayer self = Minecraft.getMinecraft().player;

            // If both players aren't null, player is visible, and player isn't dead
            if (player != null && self != null && !player.isInvisible() && !player.isDead && self.canEntityBeSeen(player) && self.getDistanceSq(player) < drawDistance * drawDistance) {
                // If not rendering self or inventory isn't open (don't render self while inventory is open)
                if(player != self || !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                    // If the player being rendered isn't this player OR the client's settings allow rendering of own glyph
                    if (!player.getUniqueID().toString().equals(self.getUniqueID().toString()) || Quickplay.INSTANCE.settings.displayOwnGlyph) {
                        // If the player has any glyphs
                        if (Quickplay.INSTANCE.glyphs.stream().anyMatch(glyph -> glyph.uuid.toString().equals(player.getUniqueID().toString()))) {
                            final PlayerGlyph glyph = Quickplay.INSTANCE.glyphs.stream().filter(thisGlyph -> thisGlyph.uuid.equals(player.getGameProfile().getId())).collect(Collectors.toList()).get(0);
                            // If this client is currently not in a game OR if the glyph is set to display in-game
                            if ((currentServer != null && !gameServerPattern.matcher(currentServer).matches()) || glyph.displayInGames)
                                renderGlyph(e.getRenderer(), glyph, e.getEntityPlayer(), e.getX(), e.getY() + offset + player.height, e.getZ());
                        }
                    }
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
    public void renderGlyph(RenderPlayer renderer, PlayerGlyph glyph, EntityPlayer player, double x, double y, double z) {

        final ResourceLocation resource = new ResourceLocation(Reference.MOD_ID, "glyphs/" + Hashing.md5().hashString(glyph.path.toString(), Charset.forName("UTF-8")).toString() + ".png");
        if(Quickplay.INSTANCE.resourcePack.resourceExists(resource) && !glyph.downloading) {
            float scale = (float) (glyph.height * 0.0015);

            // Apply GL properties
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + glyph.yOffset, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            // Rotate rendering axes
            GlStateManager.rotate(-renderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

            // Calculate x-axis rotation
            int xRotationMultiplier = 1;
            // Flip x rotation if in front-facing 3rd person
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
                xRotationMultiplier = -1;
            GlStateManager.rotate(renderer.getRenderManager().playerViewX * xRotationMultiplier, 1.0F, 0.0F, 0.0F);

            // Scale
            GlStateManager.scale(-scale, -scale, scale);

            // Draw texture
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer worldrenderer = tessellator.getBuffer();
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
        } else {
            Quickplay.INSTANCE.threadPool.submit(glyph::download);
        }
    }
}
