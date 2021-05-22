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
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import co.bugg.quickplay.wrappers.ResourceLocationWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import co.bugg.quickplay.wrappers.chat.IChatComponentWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
    public Future<?> adTextureFrameFuture;
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
        this.hypixelAnalytics = gaToken != null && gaToken.length() > 0 ? GoogleAnalyticsFactory
                .create(gaToken, Quickplay.INSTANCE.minecraft.getUuid().toString(), Reference.MOD_NAME, Reference.VERSION) : null;

        if(appData != null && appData.rewards == null) {
            appData.error = "Something went wrong... Perhaps your Quickplay is outdated. Contact bugfroggy on Discord. (0x02)";
            throw new IllegalStateException("Illegal option data! Null.");
        }

        // Get initial ad display length. Will be set to 0 later if ad is skippable
        // Default of 30 seconds if duration isn't available for some reason
        if (appData != null && appData.ad != null && appData.ad.getAsJsonObject().get("duration") != null) {
            this.totalAdTime = appData.ad.getAsJsonObject().get("duration").getAsInt() * 1000;
        } else {
            this.totalAdTime = 30000;
        }

        this.adTimerBarAnimation = new Animation(this.totalAdTime);
        Quickplay.INSTANCE.threadPool.submit(() -> this.adTimerBarAnimation.start());

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
        if(this.hypixelAnalytics != null) {
            Quickplay.INSTANCE.threadPool.submit(() -> {
                try {
                    // Whether the player has a rank or not
                    this.hypixelAnalytics.createEvent("Ranked", ((appData != null && appData.skippable) ? "Yes" : "No"))
                            .send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void hookInit() {
        super.hookInit();

        // Hack to fix this issue: https://www.minecraftforge.net/forum/topic/36866-189mouse-not-showing-up-in-gui/
        Mouse.setGrabbed(false);

        if(this.appData != null) {
            // Set scaling
            this.adScale = this.getHeight() > 300 ? 0.7 : 0.4;
            this.headerScale = this.getHeight() > 300 ? 2.5 : 2.0;

            // Set ad time to 0 if ad is skippable or there is an error
            if (((this.appData.skippable && !DailyRewardGui.forceAds) || this.appData.error != null) && this.adTimerBarAnimation != null) {
                this.adTimerBarAnimation.stop();
                this.adTimerBarAnimation.progress = 1;
            }

            this.updateState();

            int currentId = 0;
            if (this.currentState == State.ADROLL) {
                // Start swapping ad frames (if necessary)
                // Cancel the previous thread if it exists
                if (this.adTextureFrameFuture != null)
                    this.adTextureFrameFuture.cancel(true);
                // Start a new thread
                this.adTextureFrameFuture = Quickplay.INSTANCE.threadPool.submit(() -> {
                    while (Minecraft.getMinecraft().currentScreen == this && this.currentState == State.ADROLL) {
                        try {
                            Thread.sleep(DailyRewardGui.adFrameLength);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                        this.switchAdFrame();
                    }
                });
                this.componentList.add(new QuickplayGuiString(DailyRewardGui.storeUrl, currentId++, this.getWidth() / 2,
                        (int) (this.getHeight() / 2 + DailyRewardGui.adTextureSize / 2 * this.adScale), this.getWidth(), 20,
                        Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.adroll.clickToVisit"),
                        true, false));
            } else if (this.currentState == State.MENU) {

                int currentCard = 0;
                final int cardCount = this.appData.rewards.length;
                // Offset by half when card count is even - Otherwise just by missing width
                final int xOffset = (cardCount % 2 == 0 ? (this.cardWidth / 2) + (256 - this.cardWidth) / 2 : (256 - this.cardWidth) / 2);
                for (final DailyRewardOption option : this.appData.rewards) {
                    this.componentList.add(new QuickplayGuiButton(option, currentId++,
                            (int) ((this.getWidth() / 2 - (this.cardWidth + 36) * this.cardScale / 2) + ((currentCard - cardCount / 2)
                                    * (this.cardWidth * this.cardScale + this.cardMargins))) + xOffset,
                            (int) (this.getHeight() / 2 - this.cardHeight * this.cardScale / 2),
                            this.cardWidth, this.cardHeight, null, option.getTexture(), 0, 0,
                            this.cardScale, false));

                    currentCard++;
                }
            } else if (this.currentState == State.CLAIMED) {
                // Start animation
                Quickplay.INSTANCE.threadPool.submit(() -> {
                    this.claimedInitAnimation.start();
                });
                // Play claim sound
                // TODO generalize
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(Reference.MOD_ID, "card.pick"), 1.0F));
                // Add share button
                final int sharedWidth = 100;
                final int sharedHeight = 20;
                this.componentList.add(new QuickplayGuiButton("https://rewards.hypixel.net/claim-reward/shared/" +
                        this.appData.id, currentId++, this.getWidth() / 2 - sharedWidth / 2,
                        (int) (this.getHeight() / 2 + this.cardHeight / 2 * this.cardScale) + 5,
                        sharedWidth, sharedHeight, Quickplay.INSTANCE.elementController
                            .translate("quickplay.premium.ingameReward.claimed.share"), false));
            }

            // Open link button
            if (this.appData.id != null &&
                    (this.currentState == State.MENU || this.currentState == State.ADROLL || this.currentState == State.ERROR)) {
                final int linkButtonWidth = 100;
                final int linkButtonHeight = 20;
                final int linkButtonMargins = 3;
                this.componentList.add(new QuickplayGuiButton("https://rewards.hypixel.net/claim-reward/" + this.appData.id,
                        currentId++, this.getWidth() - linkButtonWidth - linkButtonMargins,
                        this.getHeight() - linkButtonHeight - linkButtonMargins, linkButtonWidth, linkButtonHeight,
                        Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.openLink"), false));
            }
        }
    }

    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {

        // Maintain the current GUI state
        if(this.updateState()) {
            this.hookInit();
        }

        if(this.adFadeAnimation != null && this.adFadeAnimation.started) {
            this.adFadeAnimation.updateFrame();
        }
        if(this.adTimerBarAnimation != null && this.adTimerBarAnimation.started) {
            this.adTimerBarAnimation.updateFrame();
        }

        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        this.drawDefaultBackground();

        if(Quickplay.INSTANCE.isEnabled) {
            // Draw legal notes
            final double legalScale = 1.0;
            GlStateManagerWrapper.scale(legalScale);
            final String[] legalLines = this.listFormattedStringToWidth(Quickplay.INSTANCE.elementController
                    .translate("quickplay.premium.ingameReward.hypixelProperty"),
                    (int) (this.getWidth() * 0.9 / legalScale)).toArray(new String[0]);
            int legalLineHeight = this.getHeight() > 300 ? 22 : 5;
            for (final String line : legalLines) {
                this.drawCenteredString(line, (int) (this.getWidth() / 2 / legalScale), legalLineHeight,
                        Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                legalLineHeight += this.getFontHeight();
            }
            GlStateManagerWrapper.scale(1 / legalScale);

            if (this.currentState == State.ERROR) {

                // Draw header
                GlStateManagerWrapper.scale(this.headerScale);
                // Table flip
                this.drawCenteredString("\u0028\u256f\u00b0\u25a1\u00b0\u0029\u256f\ufe35 \u253b\u2501\u253b",
                        (int) (this.getWidth() / 2 / this.headerScale),
                        (int) (this.getHeight() * 0.2 / this.headerScale),
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                GlStateManagerWrapper.scale(1 / this.headerScale);

                // Draw error text
                final String[] errorLines = this.listFormattedStringToWidth(this.appData.error, (int) (this.getWidth() * 0.9)).toArray(new String[0]);
                int lineHeight = (int) (this.getHeight() * 0.2 + this.getFontHeight() * this.headerScale) + 20;
                for (String line : errorLines) {
                    this.drawCenteredString(line, this.getWidth() / 2, lineHeight,
                            Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    lineHeight += this.getFontHeight();
                }

                super.hookRender(mouseX, mouseY, partialTicks);
            } else if (this.currentState == State.ADROLL) {

                // Draw header
                GlStateManagerWrapper.scale(this.headerScale);
                this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.adroll.header"),
                        (int) (this.getWidth() / 2 / this.headerScale),
                        (int) ((this.getHeight() / 2 - DailyRewardGui.adTextureSize / 2 * this.adScale)
                                / this.headerScale - 5 - this.getFontHeight()),
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
               GlStateManagerWrapper.scale(1 / this.headerScale);

                // Draw remaining time text
                final double remainingTimeScale = 1.5;
                GlStateManagerWrapper.scale(remainingTimeScale);
                this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingamereward.remaining",
                        String.valueOf(((Number) Math.ceil((this.totalAdTime * (1 - this.adTimerBarAnimation.progress)) / 1000)).intValue())),
                        (int) (this.getWidth() / remainingTimeScale), (int) (legalLineHeight / remainingTimeScale) + 2,
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                GlStateManagerWrapper.scale(1 / remainingTimeScale);

                // Draw description text
                final String[] descriptionLines = this.listFormattedStringToWidth(Quickplay.INSTANCE.elementController
                        .translate("quickplay.premium.ingameReward.adroll.text"), (int) (this.getWidth() * 0.9)).toArray(new String[0]);
                int lineHeight = (int) (this.getHeight() / 2 + DailyRewardGui.adTextureSize / 2 * this.adScale) + this.getFontHeight() + 5;
                for (final String line : descriptionLines) {
                    this.drawCenteredString(line, this.getWidth() / 2, lineHeight,
                            Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    lineHeight += this.getFontHeight();
                }

                // Draw VIP advertisement to skip
                this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.adroll.skipAd"),
                        this.getWidth() / 2, lineHeight + 5,
                        Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);

                // Draw ad loading bar
                if (this.adTimerBarAnimation != null) {
                    QuickplayGui.drawRect(0, 0, (int) (this.getWidth() * this.adTimerBarAnimation.progress), DailyRewardGui.adTimerBarHeight,
                            Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                }

                // Draw the advertisement texture
                GlStateManagerWrapper.scale(this.adScale);
                // Draw the current frame if necessary (if it's not faded out)
                if (this.adFadeAnimation != null && this.adFadeAnimation.progress < 1) {
                    GlStateManagerWrapper.enableBlend();
                    final float currentFrameOpacity = (float) (1 - this.adFadeAnimation.progress);
                    GlStateManagerWrapper.color(1, 1, 1, currentFrameOpacity);
                    Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID,
                            this.currentFrame.getTextureLocation()));
                    this.drawTexturedModalRect((float) ((this.getWidth() / 2 / this.adScale - DailyRewardGui.adTextureSize / 2)),
                            (float) ((this.getHeight() / 2 / this.adScale - DailyRewardGui.adTextureSize / 2)),
                            0, 0, DailyRewardGui.adTextureSize, DailyRewardGui.adTextureSize);
                }
                // Draw the next frame if necessary (if it's fading in)
                if (this.adFadeAnimation != null && this.adFadeAnimation.progress > 0) {
                    GlStateManagerWrapper.enableBlend();
                    final float nextFrameOpacity = (float) this.adFadeAnimation.progress;
                    GlStateManagerWrapper.color(1, 1, 1, nextFrameOpacity);
                    Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID,
                            this.currentFrame.getNext().getTextureLocation()));
                    this.drawTexturedModalRect((float) ((this.getWidth() / 2 / this.adScale - DailyRewardGui.adTextureSize / 2)),
                            (float) ((this.getHeight() / 2 / this.adScale - DailyRewardGui.adTextureSize / 2)), 0, 0,
                            DailyRewardGui.adTextureSize, DailyRewardGui.adTextureSize);
                }

                GlStateManagerWrapper.scale( 1 / this.adScale);

                super.hookRender(mouseX, mouseY, partialTicks);

            } else if (this.currentState == State.MENU) {
                if (this.appData.rewards != null) {
                    // Draw header
                    GlStateManagerWrapper.scale(this.headerScale);
                    this.drawCenteredString(Quickplay.INSTANCE.elementController
                            .translate("quickplay.premium.ingameReward.menu.header"),
                            (int) (this.getWidth() / 2 / this.headerScale),
                            (int) ((this.getHeight() / 2 - DailyRewardGui.adTextureSize / 2 * this.adScale)
                                    / this.headerScale - 10 - this.getFontHeight()),
                            Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    GlStateManagerWrapper.scale(1 / this.headerScale);

                    // Draw daily streak
                    if (this.appData.dailyStreak != null &&
                            this.appData.dailyStreak.getAsJsonObject().get("score") != null &&
                            this.appData.dailyStreak.getAsJsonObject().get("highScore") != null) {
                        final double dailyStreakScale = 1.2;
                        final int currentScore = this.appData.dailyStreak.getAsJsonObject().get("score").getAsInt();
                        final int highScore = this.appData.dailyStreak.getAsJsonObject().get("highScore").getAsInt();

                        GlStateManagerWrapper.scale(dailyStreakScale);
                        this.drawCenteredString(Quickplay.INSTANCE.elementController
                                .translate("quickplay.premium.ingameReward.menu.streak",
                                        String.valueOf(currentScore), String.valueOf(highScore)),
                                (int) (this.getWidth() / 2 / dailyStreakScale),
                                (int) ((this.getHeight() / 2 + this.cardHeight / 2 * this.cardScale + 10) / dailyStreakScale),
                                Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                        GlStateManagerWrapper.scale(1 / dailyStreakScale);
                    }

                    // Cards are components and are calculated in initGui
                    super.hookRender(mouseX, mouseY, partialTicks);

                    // Hovering text string - used to postpone hovering string drawing
                    // until the end of the frame to avoid overlapping (#47)
                    String hoverString = null;
                    // Go through cards to draw/update as necessary
                    // FIXME inefficient
                    for (final QuickplayGuiComponent component : new ArrayList<>(this.componentList)) {
                        if (component instanceof QuickplayGuiButton && component.origin instanceof DailyRewardOption) {
                            final DailyRewardOption option = (DailyRewardOption) component.origin;

                            // if hovering & currently hidden then uncover
                            if (option.hidden) {
                                if (component.isMouseHovering(this, mouseX, mouseY)) {
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
                                int color = this.getRarityColor(option.rarity);

                                // Draw chest
                                final double chestScale = 0.2;
                                final int chestWidth = 256;
                                final int chestHeight = 210;
                                final int chestOffset = 80;
                                GlStateManagerWrapper.color(1, 1, 1, 1);
                                GlStateManagerWrapper.scale(chestScale);
                                GlStateManagerWrapper.enableBlend();
                                final double chestYMultiplier = 0.23;
                                Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID,
                                        "textures/chest-" + (component.isMouseHovering(this, mouseX, mouseY) ? "open.png" : "closed.png")));
                                this.drawTexturedModalRect((int) ((component.x + (this.cardWidth * this.cardScale) / 2) / chestScale)
                                        - chestWidth / 2 - chestOffset, (int) ((component.y + this.cardHeight * this.cardScale *
                                        chestYMultiplier) / chestScale),
                                        0, 0, chestWidth, chestHeight);
                                GlStateManagerWrapper.scale(1 / chestScale);

                                // Draw amount
                                final double amountScale = 1.3;
                                GlStateManagerWrapper.scale(amountScale);
                                this.drawCenteredString(option.getFormattedAmount(),
                                        (int) ((component.x + 45) / amountScale),
                                        (int) ((component.y / amountScale) + this.cardHeight * this.cardScale * 0.535), color);
                                GlStateManagerWrapper.scale(1 / amountScale);

                                // Draw name
                                final double titleScale = 1.0;
                                GlStateManagerWrapper.scale(titleScale);
                                final List<String> titleLines = this.listFormattedStringToWidth(option.translateReward(this.i18n),
                                        (int) ((this.cardWidth - 50) * this.cardScale));
                                int titleLineIndex = 0;
                                final double nameYMultiplier = titleLines.size() > 1 ? 0.08 : 0.12;
                                for (final String line : titleLines) {
                                    this.drawCenteredString(line, (int) ((component.x + 45) / titleScale),
                                            (int) ((component.y / titleScale) + this.cardHeight * this.cardScale * nameYMultiplier)
                                                    + (titleLineIndex++ * this.getFontHeight()), color);
                                }
                                GlStateManagerWrapper.scale(1 / titleScale);

                                // Draw package data
                                if (option.translatePackageInfo(this.i18n) != null) {
                                    final double packageInfoScale = 0.8;
                                    GlStateManagerWrapper.scale(packageInfoScale);
                                    final List<String> packageInfoLines = this.listFormattedStringToWidth(option.translatePackageInfo(this.i18n),
                                            (int) ((this.cardWidth - 50) * this.cardScale));
                                    int packageInfoLineIndex = 0;
                                    final double packageInfoYMultiplier = packageInfoLines.size() > 1 ? 0.6 : 0.65;
                                    for (String line : packageInfoLines)
                                        this.drawCenteredString(line, (int) ((component.x + 45) / packageInfoScale),
                                                (int) ((component.y / packageInfoScale) + this.cardHeight * this.cardScale *
                                                        packageInfoYMultiplier) + (packageInfoLineIndex++ * this.getFontHeight()), color);
                                    GlStateManagerWrapper.scale(1 / packageInfoScale);
                                }

                                // Draw rarity
                                final double rarityScale = 1.0;
                                GlStateManagerWrapper.scale(rarityScale);
                                this.drawCenteredString( String.valueOf(option.translateRarity(this.i18n)),
                                        (int) ((component.x + 45) / rarityScale), (int) ((component.y / rarityScale) +
                                                this.cardHeight * this.cardScale * 0.85), color);
                                GlStateManagerWrapper.scale(1 / rarityScale);

                                // Draw description
                                if (component.isMouseHovering(this, mouseX, mouseY)) {
                                    hoverString = option.getRewardDescription(this.i18n);
                                }
                            }
                        }
                    }
                    // Draw hovering text if it's set
                    if (hoverString != null) {
                        this.drawHoveringText(Collections.singletonList(hoverString), mouseX, mouseY);
                    }

                } else {
                    this.appData.error = "Something went wrong... Perhaps your Quickplay is outdated. Contact bugfroggy on Discord. (0x01)";
                    throw new IllegalStateException("Illegal option data! Null.");
                }
            } else if (this.currentState == State.CLAIMED) {

                super.hookRender(mouseX, mouseY, partialTicks);

                if (this.claimedReward != null) {
                    final float cardTop = (float) (this.getHeight() / 2 / this.cardScale - this.cardHeight / 2);
                    final float cardLeft = (float) (this.getWidth() / 2 / this.cardScale - (this.cardWidth - 36) / 2);
                    final int color = getRarityColor(this.claimedReward.rarity);

                    // Draw header
                    GlStateManagerWrapper.scale(this.headerScale);
                    this.drawCenteredString( Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.claimed.header"),
                            (int) (this.getWidth() / 2 / this.headerScale), (int) ((this.getHeight() / 2 -
                                    DailyRewardGui.adTextureSize / 2 * this.adScale) / this.headerScale - 10 - this.getFontHeight()),
                            Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                    GlStateManagerWrapper.scale(1 / headerScale);

                    // Draw close string
                    this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.premium.ingameReward.claimed.close",
                            Keyboard.getKeyName(Keyboard.KEY_ESCAPE)), this.getWidth() / 2, (int) ((cardTop + this.cardHeight) * this.cardScale) + 30,
                            Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);

                    final int optionIndex = Arrays.asList(this.appData.rewards).indexOf(this.claimedReward);
                    // Cards are animated. Old cards slide off screen and picked card slides to center
                    // Selected card eases out, other cards ease in
                    // Determine offset for the moving of the card from it's original position
                    double cardAnimationOffset;
                    if (this.claimedInitAnimation != null && this.claimedInitAnimation.progress < 1 && optionIndex >= 0) {
                        cardAnimationOffset = (optionIndex - 1) * (1 - this.claimedInitAnimation.progress * (2 - this.claimedInitAnimation.progress)) * this.cardWidth;
                    } else {
                        cardAnimationOffset = 0;
                    }

                    // Draw card
                    GlStateManagerWrapper.color(1, 1, 1, 1);
                    GlStateManagerWrapper.scale(this.cardScale);
                    Quickplay.INSTANCE.minecraft.bindTexture(claimedReward.getTexture());
                    this.drawTexturedModalRect((float) (cardLeft + cardAnimationOffset), cardTop, 0, 0, this.cardWidth, this.cardHeight);
                    GlStateManagerWrapper.scale(1 / this.cardScale);

                    // Draw chest
                    final double chestScale = 0.2;
                    final int chestWidth = 256;
                    final int chestHeight = 210;
                    final int chestOffset = 80;
                    GlStateManagerWrapper.color(1, 1, 1, 1);
                    GlStateManagerWrapper.scale(chestScale, chestScale, chestScale);
                    GlStateManagerWrapper.enableBlend();
                    final double chestYMultiplier = 0.23;
                    Quickplay.INSTANCE.minecraft.bindTexture(new ResourceLocationWrapper(Reference.MOD_ID, "textures/chest-open.png"));
                    this.drawTexturedModalRect((float) (((cardLeft + this.cardWidth / 2 - chestOffset + cardAnimationOffset) * this.cardScale) / chestScale),
                            (float) ((cardTop + this.cardHeight * chestYMultiplier) * this.cardScale / chestScale), 0, 0, chestWidth, chestHeight);
                    GlStateManagerWrapper.scale(1 / chestScale);

                    // Draw amount
                    final double amountScale = 1.3;
                    GlStateManagerWrapper.scale(amountScale);
                    this.drawCenteredString(this.claimedReward.getFormattedAmount(),
                            (int) ((cardLeft + (this.cardWidth - 36) / 2 + cardAnimationOffset) * this.cardScale / amountScale),
                            (int) ((cardTop + this.cardHeight * 0.69) * this.cardScale / amountScale), color);
                    GlStateManagerWrapper.scale(1 / amountScale);

                    // Draw name
                    final double titleScale = 1.0;
                    GlStateManagerWrapper.scale(titleScale);
                    final List<String> titleLines = this.listFormattedStringToWidth(this.claimedReward.translateReward(this.i18n),
                            (int) ((this.cardWidth - 50) * this.cardScale));
                    int titleLineIndex = 0;
                    final double nameYMultiplier = titleLines.size() > 1 ? 0.08 : 0.12;
                    for (final String line : titleLines) {
                        this.drawCenteredString(line, (int) ((cardLeft + (this.cardWidth - 36) / 2 + cardAnimationOffset) * this.cardScale / titleScale),
                                (int) ((cardTop + this.cardHeight * nameYMultiplier) * this.cardScale / titleScale) + (titleLineIndex++ * this.getFontHeight()), color);
                    }
                    GlStateManagerWrapper.scale(1 / titleScale);

                    // Draw package data
                    if (this.claimedReward.translatePackageInfo(this.i18n) != null) {
                        final double packageInfoScale = 0.8;
                        GlStateManagerWrapper.scale(packageInfoScale);
                        final List<String> packageInfoLines = this.listFormattedStringToWidth(this.claimedReward.translatePackageInfo(this.i18n),
                                (int) ((this.cardWidth - 50) * this.cardScale));
                        int packageInfoLineIndex = 0;
                        final double packageInfoYMultiplier = packageInfoLines.size() > 1 ? 0.47 : 0.52;
                        for (final String line : packageInfoLines) {
                            this.drawCenteredString(line, (int) ((cardLeft + (this.cardWidth - 36) / 2 + cardAnimationOffset) *
                                            this.cardScale / packageInfoScale),
                                    (int) ((cardTop + this.cardHeight * packageInfoYMultiplier) * this.cardScale / packageInfoScale)
                                            + (packageInfoLineIndex++ * this.getFontHeight()), color);
                        }
                        GlStateManagerWrapper.scale(1 / packageInfoScale);
                    }

                    // Draw rarity
                    final double rarityScale = 1.0;
                    GlStateManagerWrapper.scale(rarityScale);
                    this.drawCenteredString(String.valueOf(this.claimedReward.translateRarity(this.i18n)),
                            (int) ((cardLeft + (this.cardWidth - 36) / 2 + cardAnimationOffset) * this.cardScale / rarityScale),
                            (int) ((cardTop + this.cardHeight * 0.85) * this.cardScale / rarityScale), color);
                    GlStateManagerWrapper.scale(1 / rarityScale);

                    // Draw falling cards animation
                    if (this.claimedInitAnimation != null && this.claimedInitAnimation.started && this.claimedInitAnimation.progress < 1) {
                        this.claimedInitAnimation.updateFrame();
                        final double progress = this.claimedInitAnimation.progress;

                        Quickplay.INSTANCE.minecraft
                                .bindTexture(new ResourceLocationWrapper(Reference.MOD_ID, "textures/card-back.png"));

                        GlStateManagerWrapper.color(1, 1, 1, 1);
                        GlStateManagerWrapper.scale(this.cardScale);

                        if (optionIndex != 0) {
                            // Draw left card
                            this.drawTexturedModalRect((float) (cardLeft - (this.cardWidth / 2) / this.cardScale) - this.cardMargins,
                                    (float) (cardTop + this.getHeight() * 0.75 * progress * progress / this.cardScale),
                                    0, 0, this.cardWidth, this.cardHeight);
                        }
                        if (optionIndex != 1) {
                            // Draw middle card
                            this.drawTexturedModalRect(cardLeft, (float) (cardTop + this.getHeight() * 0.75 * progress * progress / this.cardScale),
                                    0, 0, this.cardWidth, this.cardHeight);
                        }
                        if (optionIndex != 2) {
                            // Draw right card
                            this.drawTexturedModalRect((float) (cardLeft + this.cardWidth / 2 / this.cardScale) + this.cardMargins,
                                    (float) (cardTop + this.getHeight() * 0.75 * progress * progress / this.cardScale),
                                    0, 0, this.cardWidth, this.cardHeight);
                        }

                        GlStateManagerWrapper.scale(1 / cardScale);
                    }
                }

            } else {
                // State unknown, draw loading text
                this.drawCenteredString(Quickplay.INSTANCE.elementController
                        .translate("quickplay.premium.ingameReward.loading"),
                        this.getWidth() / 2, this.getHeight() / 2,
                        Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                super.hookRender(mouseX, mouseY, partialTicks);
            }

            for (final QuickplayGuiComponent component : this.componentList) {
                if (component.origin.equals(DailyRewardGui.storeUrl) && component.isMouseHovering(this, mouseX, mouseY)) {
                    this.drawHoveringText(Collections.singletonList(Quickplay.INSTANCE.elementController
                            .translate("quickplay.premium.ingameReward.adroll.clickToVisit")), mouseX, mouseY);
                }
            }

        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(Quickplay.INSTANCE.elementController.translate("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.getWidth() / 2, this.getHeight() / 2, 0xffffff);
        }

        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }

    /**
     * Update the current state of this GUI & re-init if necessary
     * @return whether the state has changed
     * @see State
     * @see #currentState
     */
    public boolean updateState() {
        final State oldState = this.currentState;

        if(this.appData == null || this.appData.error != null) {
            this.currentState = State.ERROR;
        } else if((this.appData.skippable && !forceAds) || (this.adTimerBarAnimation == null || this.adTimerBarAnimation.progress >= 1)) {
            if(this.claimedReward == null) {
                this.currentState = State.MENU;
            } else {
                this.currentState = State.CLAIMED;
            }
        } else {
            this.currentState = State.ADROLL;
        }

        if(oldState != this.currentState) {
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
        } else {
            return false;
        }
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
        return color | (int) (this.opacity * 255) << 24;
    }

    /**
     * Start switching ad frame by animation
     */
    public void switchAdFrame() {
        Quickplay.INSTANCE.threadPool.submit(() -> {
            if(this.adFadeAnimation != null) {
                this.adFadeAnimation.start();
                // Thread blocked until animation completes
                this.adFadeAnimation.stop();
                this.adFadeAnimation.progress = 0;
            }
            // Advance frame regardless of animation
            this.currentFrame = this.currentFrame.getNext();
        });
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        // If the store button
        if(component.origin.equals(DailyRewardGui.storeUrl)) {
            try {
                Desktop.getDesktop().browse(new URI(DailyRewardGui.storeUrl));
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
        if(this.currentState == State.MENU && this.appData != null && this.appData.rewards != null) {
            final List rewardsList = Arrays.asList(this.appData.rewards);
            if(rewardsList.contains(component.origin))
                claim(rewardsList.indexOf(component.origin));
        }

        // If the "Open URL" button or "Share" button
        if(component.origin instanceof String &&
                (component.displayString.equals(Quickplay.INSTANCE.elementController
                        .translate("quickplay.premium.ingameReward.openLink")) ||
                        component.displayString.equals(Quickplay.INSTANCE.elementController
                                .translate("quickplay.premium.ingameReward.claimed.share")))) {
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
                IChatComponentWrapper msg = new QuickplayChatComponentTranslation(
                        "quickplay.premium.ingameReward.menu.claim.serverOffline");
                msg.setStyle(new ChatStyleWrapper().apply(Formatting.RED));
                Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(msg, true));
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
