package co.bugg.quickplay.client.dailyreward;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.actions.serverbound.ClaimDailyRewardAction;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.animations.Animation;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import co.bugg.quickplay.util.analytics.GoogleAnalytics;
import co.bugg.quickplay.util.analytics.GoogleAnalyticsFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * GUI used for claiming daily reward tokens
 * Beware, this code is kinda a nightmare
 */
public class DailyRewardGui extends QuickplayGui {

    public final String securityToken;
    public final DailyRewardAppData appData;
    public final JsonObject i18n;
    public final GoogleAnalytics hypixelAnalytics;

    /**
     * Total display time of the advertisement
     */
    public int totalAdTime;
    /**
     * Current frame to be rendered in the ad texture
     */
    public AdFrames currentFrame = AdFrames.RANK;
    /**
     * Future/thread for swapping the ad texture frame
     */
    public Future adTextureFrameFuture;
    /**
     * Scale of the advertisement texture
     * Dynamic depending on screen height
     */
    public double adScale = 0.5;
    /**
     * Scale of headers in this GUI
     * Dynamic depending on screen height
     */
    public double headerScale = 2.5;
    /**
     * The scale of each card
     */
    public double cardScale = 0.5;
    /**
     * The margins between each card
     */
    public int cardMargins = 20;
    /**
     * Current display state of this GUI
     */
    public State currentState;

    /**
     * Pixel height of the timer bar at the top of the screen showing how long is left on the ad
     */
    public static final int adTimerBarHeight = 4;
    /**
     * How often in milliseconds that the current ad texture frame advances
     */
    public static final int adFrameLength = 3000;
    /**
     * The size of the ad textures, both width & height
     */
    public static final int adTextureSize = 256;
    /**
     * URL that is opened when the user visits the Hypixel store
     */
    public static final String storeUrl = "https://store.hypixel.net/?utm_source=rewards-video&utm_medium=quickplay&utm_campaign=Rewards";
    /**
     * Ad screens are forced regardless of {@link DailyRewardAppData#skippable}
     * Debug ads screen by setting this to true
     */
    public static final boolean forceAds = false;
    /**
     * Animation for advertisement fading
     */
    public Animation adFadeAnimation = new Animation(500);
    /**
     * Animation for the timer bar for the current ad
     * This contains the actual timing for the advertisement too. As a result,
     * if this is set to null at any point, the advertisement stops. This is initially set in the constructor.
     */
    public Animation adTimerBarAnimation;
    /**
     * The width in pixels of each card texture
     */
    final int cardWidth = 220;
    /**
     * The height in pixels of each card texture
     */
    final int cardHeight = 256;
    /**
     * Reward that the user has claimed, if any.
     * If not null, {@link #updateState()} sets {@link #currentState} to {@link State#CLAIMED}.
     */
    public DailyRewardOption claimedReward = null;
    /**
     * Animation for when this GUI is initialized in state {@link State#CLAIMED}.
     */
    public Animation claimedInitAnimation = new Animation(500);

    public DailyRewardGui(String securityToken, DailyRewardAppData appData, JsonObject i18n, String gaToken) {
        super();
        this.securityToken = securityToken;
        this.appData = appData;
        this.i18n = i18n;
        this.hypixelAnalytics = gaToken != null ? GoogleAnalyticsFactory.create(gaToken, Minecraft.getMinecraft().getSession().getPlayerID(), Reference.MOD_NAME, Reference.VERSION) : null;

        if(appData != null && appData.rewards == null) {
            appData.error = "Something went wrong... Perhaps your Quickplay is outdated. Contact bugfroggy on Discord. (0x02)";
            throw new IllegalStateException("Illegal option data! Null.");
        }

        // Get initial ad display length. Will be set to 0 later if ad is skippable
        // Default of 30 seconds if duration isn't available for some reason
        if (appData != null && appData.ad != null && appData.ad.getAsJsonObject().get("duration") != null)
            totalAdTime = appData.ad.getAsJsonObject().get("duration").getAsInt() * 1000;
        else
            totalAdTime = 30000;

        adTimerBarAnimation = new Animation(totalAdTime);
        Quickplay.INSTANCE.threadPool.submit(() -> adTimerBarAnimation.start());

        // Send analytical data
        if(Quickplay.INSTANCE.ga != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    Quickplay.INSTANCE.ga.createEvent("Daily Reward", "New Gui")
                            .setEventLabel(new Gson().toJson(appData))
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        // Send Hypixel analytical data
        if(hypixelAnalytics != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    // Whether the player has a rank or not
                    hypixelAnalytics.createEvent("Ranked", ((appData != null && appData.skippable) ? "Yes" : "No"))
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        // Hack to fix this issue: https://www.minecraftforge.net/forum/topic/36866-189mouse-not-showing-up-in-gui/
        Mouse.setGrabbed(false);

        if(appData != null) {
            // Set scaling
            adScale = height > 300 ? 0.7 : 0.4;
            headerScale = height > 300 ? 2.5 : 2.0;

            // Set ad time to 0 if ad is skippable or there is an error
            if (((appData.skippable && !forceAds) || appData.error != null) && adTimerBarAnimation != null) {
                adTimerBarAnimation.stop();
                adTimerBarAnimation.progress = 1;
            }

            updateState();

            int currentId = 0;
            if (currentState == State.ADROLL) {
                // Start swapping ad frames (if necessary)
                // Cancel the previous thread if it exists
                if (adTextureFrameFuture != null)
                    adTextureFrameFuture.cancel(true);
                // Start a new thread
                adTextureFrameFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
                    while (Minecraft.getMinecraft().currentScreen == this && currentState == State.ADROLL) {
                        try {
                            Thread.sleep(adFrameLength);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                        switchAdFrame();
                    }
                });
                this.componentList.add(new QuickplayGuiString(storeUrl, currentId++, width / 2, (int) (height / 2 + adTextureSize / 2 * adScale), width, 20, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.adroll.clickToVisit"), true, false));
            } else if (currentState == State.MENU) {

                int currentCard = 0;
                final int cardCount = appData.rewards.length;
                // Offset by half when card count is even - Otherwise just by missing width
                final int xOffset = (cardCount % 2 == 0 ? (cardWidth / 2) + (256 - cardWidth) / 2 : (256 - cardWidth) / 2);
                for (DailyRewardOption option : appData.rewards) {
                    this.componentList.add(new QuickplayGuiButton(option, currentId++, (int) ((width / 2 - (cardWidth + 36) * cardScale / 2) + ((currentCard - cardCount / 2) * (cardWidth * cardScale + cardMargins))) + xOffset, (int) (height / 2 - cardHeight * cardScale / 2), cardWidth, cardHeight, null, option.getTexture(), 0, 0, cardScale, false));
                    currentCard++;
                }
            } else if (currentState == State.CLAIMED) {
                // Start animation
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    claimedInitAnimation.start();
                });
                // Play claim sound
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(Reference.MOD_ID, "card.pick"), 1.0F));
                // Add share button
                final int sharedWidth = 100;
                final int sharedHeight = 20;
                this.componentList.add(new QuickplayGuiButton("https://rewards.hypixel.net/claim-reward/shared/" + appData.id, currentId++, width / 2 - sharedWidth / 2, (int) (height / 2 + cardHeight / 2 * cardScale) + 5, sharedWidth, sharedHeight, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.claimed.share"), false));
            }

            // Open link button
            if (appData.id != null && (currentState == State.MENU || currentState == State.ADROLL || currentState == State.ERROR)) {
                final int linkButtonWidth = 100;
                final int linkButtonHeight = 20;
                final int linkButtonMargins = 3;
                this.componentList.add(new QuickplayGuiButton("https://rewards.hypixel.net/claim-reward/" + appData.id, currentId++, width - linkButtonWidth - linkButtonMargins, height - linkButtonHeight - linkButtonMargins, linkButtonWidth, linkButtonHeight, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.openLink"), false));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Maintain the current GUI state
        if(updateState()) {
            initGui();
        }

        if(adFadeAnimation != null && adFadeAnimation.started) {
            adFadeAnimation.updateFrame();
        }
        if(adTimerBarAnimation != null && adTimerBarAnimation.started) {
            adTimerBarAnimation.updateFrame();
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        if(Quickplay.INSTANCE.isEnabled) {
            // Draw legal notes
            final double legalScale = 1.0;
            GL11.glScaled(legalScale, legalScale, legalScale);
            final String[] legalLines = fontRendererObj.listFormattedStringToWidth(Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.hypixelProperty"), (int) (width * 0.9 / legalScale)).toArray(new String[0]);
            int legalLineHeight = height > 300 ? 22 : 5;
            for (String line : legalLines) {
                drawCenteredString(fontRendererObj, line, (int) (width / 2 / legalScale), legalLineHeight, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                legalLineHeight += fontRendererObj.FONT_HEIGHT;
            }
            GL11.glScaled(1 / legalScale, 1 / legalScale, 1 / legalScale);

            if (currentState == State.ERROR) {

                // Draw header
                GL11.glScaled(headerScale, headerScale, headerScale);
                drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.error.header"), (int) (width / 2 / headerScale), (int) (height * 0.2 / headerScale), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

                // Draw error text
                final String[] errorLines = fontRendererObj.listFormattedStringToWidth(appData.error, (int) (width * 0.9)).toArray(new String[0]);
                int lineHeight = (int) (height * 0.2 + fontRendererObj.FONT_HEIGHT * headerScale) + 20;
                for (String line : errorLines) {
                    drawCenteredString(fontRendererObj, line, width / 2, lineHeight, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                    lineHeight += fontRendererObj.FONT_HEIGHT;
                }

                super.drawScreen(mouseX, mouseY, partialTicks);
            } else if (currentState == State.ADROLL) {

                // Draw header
                GL11.glScaled(headerScale, headerScale, headerScale);
                drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.adroll.header"), (int) (width / 2 / headerScale), (int) ((height / 2 - adTextureSize / 2 * adScale) / headerScale - 5 - fontRendererObj.FONT_HEIGHT), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

                // Draw remaining time text
                final double remainingTimeScale = 1.5;
                GL11.glScaled(remainingTimeScale, remainingTimeScale, remainingTimeScale);
                drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingamereward.remaining", String.valueOf(((Number) Math.ceil((totalAdTime * (1 - adTimerBarAnimation.progress)) / 1000)).intValue())), (int) (width / 2 / remainingTimeScale), (int) (legalLineHeight / remainingTimeScale) + 2, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                GL11.glScaled(1 / remainingTimeScale, 1 / remainingTimeScale, 1 / remainingTimeScale);

                // Draw description text
                final String[] descriptionLines = fontRendererObj.listFormattedStringToWidth(Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.adroll.text"), (int) (width * 0.9)).toArray(new String[0]);
                int lineHeight = (int) (height / 2 + adTextureSize / 2 * adScale) + fontRendererObj.FONT_HEIGHT + 5;
                for (String line : descriptionLines) {
                    drawCenteredString(fontRendererObj, line, width / 2, lineHeight, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                    lineHeight += fontRendererObj.FONT_HEIGHT;
                }

                // Draw VIP advertisement to skip
                drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.adroll.skipAd"), width / 2, lineHeight + 5, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

                // Draw ad loading bar
                if (adTimerBarAnimation != null) {
                    drawRect(0, 0, (int) (width * adTimerBarAnimation.progress), adTimerBarHeight, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                }

                // Draw the advertisement texture
                GL11.glScaled(adScale, adScale, adScale);
                // Draw the current frame if necessary (if it's not faded out)
                if (adFadeAnimation != null && adFadeAnimation.progress < 1) {
                    GL11.glEnable(GL11.GL_BLEND);
                    final float currentFrameOpacity = (float) (1 - adFadeAnimation.progress);
                    GL11.glColor4f(1, 1, 1, currentFrameOpacity);
                    mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, currentFrame.getTextureLocation()));
                    drawTexturedModalRect((float) ((width / 2 / adScale - adTextureSize / 2)), (float) ((height / 2 / adScale - adTextureSize / 2)), 0, 0, adTextureSize, adTextureSize);
                }
                // Draw the next frame if necessary (if it's fading in)
                if (adFadeAnimation != null && adFadeAnimation.progress > 0) {
                    GL11.glEnable(GL11.GL_BLEND);
                    final float nextFrameOpacity = (float) adFadeAnimation.progress;
                    GL11.glColor4f(1, 1, 1, nextFrameOpacity);
                    mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, currentFrame.getNext().getTextureLocation()));
                    drawTexturedModalRect((float) ((width / 2 / adScale - adTextureSize / 2)), (float) ((height / 2 / adScale - adTextureSize / 2)), 0, 0, adTextureSize, adTextureSize);
                }

                GL11.glScaled(1 / adScale, 1 / adScale, 1 / adScale);

                super.drawScreen(mouseX, mouseY, partialTicks);

            } else if (currentState == State.MENU) {
                if (appData.rewards != null) {
                    // Draw header
                    GL11.glScaled(headerScale, headerScale, headerScale);
                    drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.menu.header"), (int) (width / 2 / headerScale), (int) ((height / 2 - adTextureSize / 2 * adScale) / headerScale - 10 - fontRendererObj.FONT_HEIGHT), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                    GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

                    // Draw daily streak
                    if (appData.dailyStreak != null && appData.dailyStreak.getAsJsonObject().get("score") != null && appData.dailyStreak.getAsJsonObject().get("highScore") != null) {
                        final double dailyStreakScale = 1.2;
                        final int currentScore = appData.dailyStreak.getAsJsonObject().get("score").getAsInt();
                        final int highScore = appData.dailyStreak.getAsJsonObject().get("highScore").getAsInt();

                        GL11.glScaled(dailyStreakScale, dailyStreakScale, dailyStreakScale);
                        drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.menu.streak", String.valueOf(currentScore), String.valueOf(highScore)), (int) (width / 2 / dailyStreakScale), (int) ((height / 2 + cardHeight / 2 * cardScale + 10) / dailyStreakScale), Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                        GL11.glScaled(1 / dailyStreakScale, 1 / dailyStreakScale, 1 / dailyStreakScale);
                    }

                    // Cards are components and are calculated in initGui
                    super.drawScreen(mouseX, mouseY, partialTicks);

                    // Hovering text string - used to postpone hovering string drawing
                    // until the end of the frame to avoid overlapping (#47)
                    String hoverString = null;
                    // Go through cards to draw/update as necessary
                    for (QuickplayGuiComponent component : componentList) {
                        if (component instanceof QuickplayGuiButton && component.origin instanceof DailyRewardOption) {
                            final DailyRewardOption option = (DailyRewardOption) component.origin;

                            // if hovering & currently hidden then uncover
                            if (option.hidden) {
                                if (component.mouseHovering(this, mouseX, mouseY)) {
                                    option.show();

                                    try {
                                        // Update texture
                                        final QuickplayGuiButton button = (QuickplayGuiButton) component;
                                        final Field field = button.getClass().getDeclaredField("texture");
                                        field.setAccessible(true);
                                        field.set(button, option.getTexture());
                                    } catch (IllegalAccessException | NoSuchFieldException e) {
                                        e.printStackTrace();
                                        Quickplay.INSTANCE.sendExceptionRequest(e);
                                    }
                                }
                            } else {
                                // Not hidden, draw text & such
                                int color = getRarityColor(option.rarity);

                                // Draw chest
                                final double chestScale = 0.2;
                                final int chestWidth = 256;
                                final int chestHeight = 210;
                                final int chestOffset = 80;
                                GL11.glColor4f(1, 1, 1, 1);
                                GL11.glScaled(chestScale, chestScale, chestScale);
                                GL11.glEnable(GL11.GL_BLEND);
                                final double chestYMultiplier = 0.23;
                                mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, "textures/chest-" + (component.mouseHovering(this, mouseX, mouseY) ? "open.png" : "closed.png")));
                                drawTexturedModalRect((int) ((component.x + (cardWidth * cardScale) / 2) / chestScale) - chestWidth / 2 - chestOffset, (int) ((component.y + cardHeight * cardScale * chestYMultiplier) / chestScale), 0, 0, chestWidth, chestHeight);
                                GL11.glScaled(1 / chestScale, 1 / chestScale, 1 / chestScale);

                                // Draw amount
                                final double amountScale = 1.3;
                                GL11.glScaled(amountScale, amountScale, amountScale);
                                drawCenteredString(fontRendererObj, option.getFormattedAmount(), (int) ((component.x + 45) / amountScale), (int) ((component.y / amountScale) + cardHeight * cardScale * 0.535), color);
                                GL11.glScaled(1 / amountScale, 1 / amountScale, 1 / amountScale);

                                // Draw name
                                final double titleScale = 1.0;
                                GL11.glScaled(titleScale, titleScale, titleScale);
                                final List<String> titleLines = fontRendererObj.listFormattedStringToWidth(option.translateReward(i18n), (int) ((cardWidth - 50) * cardScale));
                                int titleLineIndex = 0;
                                final double nameYMultiplier = titleLines.size() > 1 ? 0.08 : 0.12;
                                for (String line : titleLines) {
                                    drawCenteredString(fontRendererObj, line, (int) ((component.x + 45) / titleScale), (int) ((component.y / titleScale) + cardHeight * cardScale * nameYMultiplier) + (titleLineIndex++ * fontRendererObj.FONT_HEIGHT), color);
                                }
                                GL11.glScaled(1 / titleScale, 1 / titleScale, 1 / titleScale);

                                // Draw package data
                                if (option.translatePackageInfo(i18n) != null) {
                                    final double packageInfoScale = 0.8;
                                    GL11.glScaled(packageInfoScale, packageInfoScale, packageInfoScale);
                                    final List<String> packageInfoLines = fontRendererObj.listFormattedStringToWidth(option.translatePackageInfo(i18n), (int) ((cardWidth - 50) * cardScale));
                                    int packageInfoLineIndex = 0;
                                    final double packageInfoYMultiplier = packageInfoLines.size() > 1 ? 0.6 : 0.65;
                                    for (String line : packageInfoLines)
                                        drawCenteredString(fontRendererObj, line, (int) ((component.x + 45) / packageInfoScale), (int) ((component.y / packageInfoScale) + cardHeight * cardScale * packageInfoYMultiplier) + (packageInfoLineIndex++ * fontRendererObj.FONT_HEIGHT), color);
                                    GL11.glScaled(1 / packageInfoScale, 1 / packageInfoScale, 1 / packageInfoScale);
                                }

                                // Draw rarity
                                final double rarityScale = 1.0;
                                GL11.glScaled(rarityScale, rarityScale, rarityScale);
                                drawCenteredString(fontRendererObj, String.valueOf(option.translateRarity(i18n)), (int) ((component.x + 45) / rarityScale), (int) ((component.y / rarityScale) + cardHeight * cardScale * 0.85), color);
                                GL11.glScaled(1 / rarityScale, 1 / rarityScale, 1 / rarityScale);

                                // Draw description
                                if (component.mouseHovering(this, mouseX, mouseY)) {
                                    hoverString = option.getRewardDescription(i18n);
                                }
                            }
                        }
                    }
                    // Draw hovering text if it's set
                    if (hoverString != null) {
                        drawHoveringText(Collections.singletonList(hoverString), mouseX, mouseY);
                    }

                } else {
                    appData.error = "Something went wrong... Perhaps your Quickplay is outdated. Contact bugfroggy on Discord. (0x01)";
                    throw new IllegalStateException("Illegal option data! Null.");
                }
            } else if (currentState == State.CLAIMED) {

                super.drawScreen(mouseX, mouseY, partialTicks);

                if (claimedReward != null) {
                    final float cardTop = (float) (height / 2 / cardScale - cardHeight / 2);
                    final float cardLeft = (float) (width / 2 / cardScale - (cardWidth - 36) / 2);
                    final int color = getRarityColor(claimedReward.rarity);

                    // Draw header
                    GL11.glScaled(headerScale, headerScale, headerScale);
                    drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.claimed.header"), (int) (width / 2 / headerScale), (int) ((height / 2 - adTextureSize / 2 * adScale) / headerScale - 10 - fontRendererObj.FONT_HEIGHT), Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                    GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

                    // Draw close string
                    drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.claimed.close", Keyboard.getKeyName(Keyboard.KEY_ESCAPE)), width / 2, (int) ((cardTop + cardHeight) * cardScale) + 30, Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);

                    final int optionIndex = Arrays.asList(appData.rewards).indexOf(claimedReward);
                    // Cards are animated. Old cards slide off screen and picked card slides to center
                    // Selected card eases out, other cards ease in
                    // Determine offset for the moving of the card from it's original position
                    double cardAnimationOffset;
                    if (claimedInitAnimation != null && claimedInitAnimation.progress < 1 && optionIndex >= 0) {
                        cardAnimationOffset = (optionIndex - 1) * (1 - claimedInitAnimation.progress * (2 - claimedInitAnimation.progress)) * cardWidth;
                    } else {
                        cardAnimationOffset = 0;
                    }

                    // Draw card
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glScaled(cardScale, cardScale, cardScale);
                    mc.getTextureManager().bindTexture(claimedReward.getTexture());
                    drawTexturedModalRect((float) (cardLeft + cardAnimationOffset), cardTop, 0, 0, cardWidth, cardHeight);
                    GL11.glScaled(1 / cardScale, 1 / cardScale, 1 / cardScale);

                    // Draw chest
                    final double chestScale = 0.2;
                    final int chestWidth = 256;
                    final int chestHeight = 210;
                    final int chestOffset = 80;
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glScaled(chestScale, chestScale, chestScale);
                    GL11.glEnable(GL11.GL_BLEND);
                    final double chestYMultiplier = 0.23;
                    mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, "textures/chest-open.png"));
                    drawTexturedModalRect((float) (((cardLeft + cardWidth / 2 - chestOffset + cardAnimationOffset) * cardScale) / chestScale), (float) ((cardTop + cardHeight * chestYMultiplier) * cardScale / chestScale), 0, 0, chestWidth, chestHeight);
                    GL11.glScaled(1 / chestScale, 1 / chestScale, 1 / chestScale);

                    // Draw amount
                    final double amountScale = 1.3;
                    GL11.glScaled(amountScale, amountScale, amountScale);
                    drawCenteredString(fontRendererObj, claimedReward.getFormattedAmount(), (int) ((cardLeft + (cardWidth - 36) / 2 + cardAnimationOffset) * cardScale / amountScale), (int) ((cardTop + cardHeight * 0.69) * cardScale / amountScale), color);
                    GL11.glScaled(1 / amountScale, 1 / amountScale, 1 / amountScale);

                    // Draw name
                    final double titleScale = 1.0;
                    GL11.glScaled(titleScale, titleScale, titleScale);
                    final List<String> titleLines = fontRendererObj.listFormattedStringToWidth(claimedReward.translateReward(i18n), (int) ((cardWidth - 50) * cardScale));
                    int titleLineIndex = 0;
                    final double nameYMultiplier = titleLines.size() > 1 ? 0.08 : 0.12;
                    for (String line : titleLines) {
                        drawCenteredString(fontRendererObj, line, (int) ((cardLeft + (cardWidth - 36) / 2 + cardAnimationOffset) * cardScale / titleScale), (int) ((cardTop + cardHeight * nameYMultiplier) * cardScale / titleScale) + (titleLineIndex++ * fontRendererObj.FONT_HEIGHT), color);
                    }
                    GL11.glScaled(1 / titleScale, 1 / titleScale, 1 / titleScale);

                    // Draw package data
                    if (claimedReward.translatePackageInfo(i18n) != null) {
                        final double packageInfoScale = 0.8;
                        GL11.glScaled(packageInfoScale, packageInfoScale, packageInfoScale);
                        final List<String> packageInfoLines = fontRendererObj.listFormattedStringToWidth(claimedReward.translatePackageInfo(i18n), (int) ((cardWidth - 50) * cardScale));
                        int packageInfoLineIndex = 0;
                        final double packageInfoYMultiplier = packageInfoLines.size() > 1 ? 0.47 : 0.52;
                        for (String line : packageInfoLines) {
                            drawCenteredString(fontRendererObj, line, (int) ((cardLeft + (cardWidth - 36) / 2 + cardAnimationOffset) * cardScale / packageInfoScale), (int) ((cardTop + cardHeight * packageInfoYMultiplier) * cardScale / packageInfoScale) + (packageInfoLineIndex++ * fontRendererObj.FONT_HEIGHT), color);
                        }
                        GL11.glScaled(1 / packageInfoScale, 1 / packageInfoScale, 1 / packageInfoScale);
                    }

                    // Draw rarity
                    final double rarityScale = 1.0;
                    GL11.glScaled(rarityScale, rarityScale, rarityScale);
                    drawCenteredString(fontRendererObj, String.valueOf(claimedReward.translateRarity(i18n)), (int) ((cardLeft + (cardWidth - 36) / 2 + cardAnimationOffset) * cardScale / rarityScale), (int) ((cardTop + cardHeight * 0.85) * cardScale / rarityScale), color);
                    GL11.glScaled(1 / rarityScale, 1 / rarityScale, 1 / rarityScale);

                    // Draw falling cards animation
                    if (claimedInitAnimation != null && claimedInitAnimation.started && claimedInitAnimation.progress < 1) {
                        claimedInitAnimation.updateFrame();
                        final double progress = claimedInitAnimation.progress;

                        mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, "textures/card-back.png"));

                        GL11.glColor4f(1, 1, 1, 1);
                        GL11.glScaled(cardScale, cardScale, cardScale);

                        if (optionIndex != 0) {
                            // Draw left card
                            drawTexturedModalRect((float) (cardLeft - (cardWidth / 2) / cardScale) - cardMargins, (float) (cardTop + height * 0.75 * progress * progress / cardScale), 0, 0, cardWidth, cardHeight);
                        }
                        if (optionIndex != 1) {
                            // Draw middle card
                            drawTexturedModalRect(cardLeft, (float) (cardTop + height * 0.75 * progress * progress / cardScale), 0, 0, cardWidth, cardHeight);
                        }
                        if (optionIndex != 2) {
                            // Draw right card
                            drawTexturedModalRect((float) (cardLeft + cardWidth / 2 / cardScale) + cardMargins, (float) (cardTop + height * 0.75 * progress * progress / cardScale), 0, 0, cardWidth, cardHeight);
                        }

                        GL11.glScaled(1 / cardScale, 1 / cardScale, 1 / cardScale);
                    }
                }

            } else {
                // State unknown, draw loading text
                drawCenteredString(fontRendererObj, Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.loading"), width / 2, height / 2, Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
                super.drawScreen(mouseX, mouseY, partialTicks);
            }

            for (QuickplayGuiComponent component : componentList) {
                if (component.origin.equals(storeUrl) && component.mouseHovering(this, mouseX, mouseY)) {
                    drawHoveringText(Collections.singletonList(Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.adroll.clickToVisit")), mouseX, mouseY);
                }
            }

        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(this.fontRendererObj,
                    Quickplay.INSTANCE.translator.get("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.width / 2, this.height / 2, 0xffffff);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * Update the current state of this GUI & re-init if necessary
     * @return whether the state has changed
     * @see State
     * @see #currentState
     */
    public boolean updateState() {
        final State oldState = currentState;

        if(appData == null || appData.error != null)
            currentState = State.ERROR;
        else if((appData.skippable && !forceAds) || (adTimerBarAnimation == null || adTimerBarAnimation.progress >= 1))
            if(claimedReward == null)
                currentState = State.MENU;
            else
                currentState = State.CLAIMED;
        else
            currentState = State.ADROLL;

        if(oldState != currentState) {
            // Send analytical data
            if (Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Daily Reward", "State Change")
                                .setEventLabel(currentState.toString())
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            return true;
        } else return false;
    }

    /**
     * Get the color code for the specified rarity. White if unknown or null.
     * @param rarity String containing the rarity
     * @return Color code
     */
    public int getRarityColor(String rarity) {
        int color;
        switch (String.valueOf(rarity).toLowerCase()) {
            default:
            case "common":
                color = 0xFFFFFF;
                break;
            case "rare":
                color = 0x00D8E0;
                break;
            case "epic":
                color = 0x9900E0;
                break;
            case "legendary":
                color = 0xE0B300;
                break;
        }
        // Add opacity
        return color | (int) (opacity * 255) << 24;
    }

    /**
     * Start switching ad frame by animation
     */
    public void switchAdFrame() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(adFadeAnimation != null) {
                adFadeAnimation.start();
                // Thread blocked until animation completes
                adFadeAnimation.stop();
                adFadeAnimation.progress = 0;
            }
            // Advance frame regardless of animation
            currentFrame = currentFrame.getNext();
        });
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        // If the store button
        if(component.origin.equals(storeUrl)) {
            try {
                Desktop.getDesktop().browse(new URI(storeUrl));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }

            // Send analytical data
            if(Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Daily Reward", "Store Opened")
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // If in menu state & the component clicked is a reward
        if(currentState == State.MENU && appData != null && appData.rewards != null) {
            final List rewardsList = Arrays.asList(appData.rewards);
            if(rewardsList.contains(component.origin))
                claim(rewardsList.indexOf(component.origin));
        }

        // If the "Open URL" button or "Share" button
        if(component.origin instanceof String &&
                (component.displayString.equals(Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.openLink")) || component.displayString.equals(Quickplay.INSTANCE.translator.get("quickplay.premium.ingameReward.claimed.share")))) {
            try {
                Desktop.getDesktop().browse(new URI((String) component.origin));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                Quickplay.INSTANCE.sendExceptionRequest(e);
            }

            // Send analytical data
            if(Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Daily Reward", "URL Button Pressed")
                                .setEventLabel(String.valueOf(component.origin))
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

    }

    /**
     * Claim the specified reward
     * @param option Reward to claim
     */
    public void claim(int option) {
        if(this.appData != null && this.appData.rewards != null) {

            // Don't claim if any of the rewards aren't uncovered
            for(DailyRewardOption loopedOption : this.appData.rewards) {
                if(loopedOption.hidden)
                    return;
            }

            // Cannot claim card outside of range
            if(option < 0 || option >= this.appData.rewards.length)
                throw new IllegalArgumentException("Provided option index could not be found! Must be between 0 and rewards count.");

            this.claimedReward = this.appData.rewards[option];

            // Submit claim request
            try {
                Quickplay.INSTANCE.socket.sendAction(
                        new ClaimDailyRewardAction(option, this.securityToken, this.appData)
                );
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
                IChatComponent msg = new QuickplayChatComponentTranslation(
                        "quickplay.premium.ingameReward.menu.claim.error");
                msg.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
                Quickplay.INSTANCE.messageBuffer.push(new Message(msg, true));
            }

            // Send analytical data
            if(Quickplay.INSTANCE.ga != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        Quickplay.INSTANCE.ga.createEvent("Daily Reward", "Reward Claimed")
                                .setEventLabel(new Gson().toJson(this.claimedReward))
                                .setEventValue(option)
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            // Send Hypixel analytical data
            if(this.hypixelAnalytics != null) {
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    try {
                        // Whether the player has a rank or not
                        this.hypixelAnalytics.createEvent("Ad", "claimed")
                                .send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * The state of this GUI
     */
    public enum State {
        ERROR,
        ADROLL,
        MENU,
        CLAIMED
    }

    /**
     * An enum containing all the frames in the texture advertisement
     * TODO alter this system, opt for a time-based system allowing for animations.
     */
    private enum AdFrames {
        RANK("textures/ad-rank.png"),
        COMPANION("textures/ad-companion.png"),
        MORE("textures/ad-more.png");

        private String textureLocation;

        AdFrames(String textureLocation) {
            this.textureLocation = textureLocation;
        }

        public String getTextureLocation() {
            return textureLocation;
        }

        public String toString() {
            return name();
        }

        public AdFrames getNext() {
            if(ordinal() + 1 == AdFrames.values().length)
                return AdFrames.values()[0];
            else
                return AdFrames.values()[ordinal() + 1];
        }
    }
}
