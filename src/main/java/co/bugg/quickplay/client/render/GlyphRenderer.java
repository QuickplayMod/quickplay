package co.bugg.quickplay.client.render;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.animations.Animation;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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

    /**
     * Animation list for each player with a glyph and their glyph's opacity.
     * This is used to determine glyph opacity from fading due to proximity.
     */
    private final Map<UUID, Animation> opacityAnimations = new HashMap<>();
    /**
     * The distance to the client that the player which the UUID key belongs to was last frame.
     * This is used in determining when to start a fade animation due to proximity.
     */
    private final Map<UUID, Double> prevDistance = new HashMap<>();

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post e) {
        // Don't render at all if F1 is hit or if the client is in a game (or unknown location)
        if(!Minecraft.getMinecraft().gameSettings.hideGUI) {
            final String currentServer = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
            final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
            final EntityPlayer player = e.entityPlayer;

            // Stop if either player is null
            if (player == null || self == null) {
                return;
            }

            // Stop if client user cannot see the player/the player's name
            if (player.isInvisibleToPlayer(self) || player.isDead || player.isSneaking() || !self.canEntityBeSeen(player)) {
                return;
            }
            // Stop if the player is farther than the draw distance of glyphs
            if (self.getDistanceSqToEntity(player) > GlyphRenderer.drawDistance * GlyphRenderer.drawDistance) {
                return;
            }
            // Stop if we're rendering the client player, and they have their inventory open. Otherwise the glyph
            // appears in the inventory.
            if (player == self && Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                return;
            }
            // Stop if the client user has disabled rendering their own glyph, and we're rendering the client user.
            if (player.getUniqueID().toString().equals(self.getUniqueID().toString()) && !Quickplay.INSTANCE.settings.displayOwnGlyph) {
                return;
            }

            for (PlayerGlyph glyph : Quickplay.INSTANCE.glyphs) {
                if (!glyph.uuid.toString().equals(player.getUniqueID().toString())) {
                    continue;
                }
                // Don't display glyph if the glyph owner has disabled displaying the glyph in-game.
                if (currentServer == null || (gameServerPattern.matcher(currentServer).matches() && !glyph.displayInGames)) {
                    return;
                }
                float opacity = 1.0f;
                if (!player.getUniqueID().toString().equals(self.getUniqueID().toString())) {
                    double xDist = Math.abs(player.posX - self.posX);
                    double yDist = Math.abs(player.posY - self.posY);
                    double zDist = Math.abs(player.posZ - self.posZ);
                    double pythagorean = Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2) + Math.pow(zDist, 2));

                    opacity = this.calcOpacityForPlayer(player, pythagorean,
                            gameServerPattern.matcher(currentServer).matches() ? 8 : 4);
                }
                renderGlyph(e.renderer, glyph, e.x, e.y + offset + player.height, e.z, opacity);
            }

        }
    }

    /**
     * Calculate the opacity of a glyph for a specified user at a specified distance.
     * @param player The target player.
     * @param dist The distance the target player is from the client player.
     * @param horizon The "horizon point" at which glyphs should appear/disappear.
     */
    private float calcOpacityForPlayer(EntityPlayer player, double dist, double horizon) {
        final int animationMillis = 200;
        final UUID uuid = player.getGameProfile().getId();
        Animation currentAnim = this.opacityAnimations.get(uuid);
        final Double prevDistance = this.prevDistance.get(uuid);
        float opacity;

        // If either animation or previous distance are null, OR if the player has crossed the distance horizon since last frame
        if(currentAnim == null || prevDistance == null ||
                (prevDistance <= horizon && dist > horizon) || (prevDistance > horizon && dist <= horizon)) {
            currentAnim = new Animation(animationMillis);
            this.opacityAnimations.put(uuid, currentAnim);
            Quickplay.INSTANCE.threadPool.submit(currentAnim::start);
        }
        // Since the animation is started in a separate thread, it might not have started yet for one or two frames.
        if(currentAnim.started) {
            currentAnim.updateFrame();
            opacity = (float) currentAnim.progress;
        } else {
            opacity = 0.0f;
        }

        // Flip opacity if the user is within the horizon distance
        if(dist <= horizon) {
            opacity = 1 - opacity;
        }
        this.prevDistance.put(uuid, dist);
        return opacity;
    }

    /**
     * Render a glyph
     * @param renderer Renderer to use
     * @param glyph Glyph to render
     * @param x x position
     * @param y y position
     * @param z z position
     * @param opacity Opacity to render the glyph at
     */
    public synchronized void renderGlyph(RenderPlayer renderer, PlayerGlyph glyph, double x, double y, double z, float opacity) {

        final ResourceLocation resource = new ResourceLocation(Reference.MOD_ID, "glyphs/" +
                Hashing.md5().hashString(glyph.path.toString(), StandardCharsets.UTF_8).toString() + ".png");
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
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
                xRotationMultiplier = -1;
            }
            GlStateManager.rotate(renderer.getRenderManager().playerViewX * xRotationMultiplier, 1.0F, 0.0F, 0.0F);

            // Scale
            GlStateManager.scale(-scale, -scale, scale);

            // Opacity
            GlStateManager.color(1, 1, 1, opacity);

            // Draw texture
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            renderer.bindTexture(resource);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-16, -16, 0.0D).tex(0, 0).endVertex();
            worldrenderer.pos(-16, 16, 0.0D).tex(0, 1).endVertex();
            worldrenderer.pos(16, 16, 0.0D).tex(1, 1).endVertex();
            worldrenderer.pos(16, -16, 0.0D).tex(1, 0).endVertex();
            tessellator.draw();

            GlStateManager.color(1,1,1,1);
            GlStateManager.scale(1/-scale, 1/-scale, 1/scale);

            // Remove GL properties
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else {
            Quickplay.INSTANCE.threadPool.submit(glyph::download);
        }
    }
}
