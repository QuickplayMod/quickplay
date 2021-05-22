package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiSlider;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.wrappers.GlStateManagerWrapper;
import co.bugg.quickplay.wrappers.chat.ChatStyleWrapper;
import co.bugg.quickplay.wrappers.chat.Formatting;
import net.minecraft.client.gui.GuiPageButtonList;

import java.awt.*;
import java.io.IOException;

/**
 * Gui for editing Quickplay colors
 */
public class QuickplayGuiEditColor extends QuickplayGui {

    /**
     * Color this GUI is editing
     */
    public QuickplayColor color;
    /**
     * Name of the color being edited
     */
    public String colorName;
    /**
     * Configuration this color belongs to
     */
    public AConfiguration config;
    /**
     * GUI this client was previously on
     */
    public QuickplayGui previousGui;

    /**
     * The Y location of the sample text
     */
    public int sampleTextY;
    /**
     * The scale of the sample text
     */
    public double sampleTextScale;
    /**
     * The Y location of the name text
     */
    public int nameTextY;
    /**
     * The scale of the name text
     */
    public double nameTextScale;

    /**
     * Vertical margins between each button/element on the screen
     */
    public static int elementMargins = 4;
    /**
     * The height of each button/element on the screen
     */
    public static int elementHeight = 20;
    /**
     * The maximum arbitrary speed the client may set their chroma colors to
     */
    public static float chromaMaxSpeed = 0.05f;

    /**
     * Constructor
     *
     * @param color Color the client is editing
     * @param colorName Display name of this color
     * @param config Configuration this color is coming from
     */
    public QuickplayGuiEditColor(QuickplayColor color, String colorName, AConfiguration config) {
        this(color, colorName, config, null);
    }

    /**
     * Constructor
     *
     * @param color Color the client is editing
     * @param colorName Display name of this color
     * @param config The config this color is coming from
     * @param previousGui Previous GUI the client was on
     */
    public QuickplayGuiEditColor(QuickplayColor color, String colorName, AConfiguration config, QuickplayGui previousGui) {
        this.color = color;
        this.colorName = colorName;
        this.config = config;
        this.previousGui = previousGui;
    }



    @Override
    public void hookRender(int mouseX, int mouseY, float partialTicks) {
        GlStateManagerWrapper.pushMatrix();
        GlStateManagerWrapper.enableBlend();

        /*
         * Draw background
         */
        this.drawDefaultBackground();

        if(Quickplay.INSTANCE.isEnabled) {
            // Draw text
            if (this.opacity > 0) {
                GlStateManagerWrapper.scale(this.nameTextScale);
                this.drawCenteredString(this.colorName, (int) (this.getWidth() / 2 / this.nameTextScale),
                        (int) (this.nameTextY / this.nameTextScale), 0xFFFFFF);
                GlStateManagerWrapper.scale(1 / this.nameTextScale);

                GlStateManagerWrapper.scale(this.sampleTextScale);
                this.drawCenteredString(Quickplay.INSTANCE.elementController
                                .translate("quickplay.config.color.gui.sampletext"),
                        (int) (this.getWidth() / 2 / this.sampleTextScale), (int) (this.sampleTextY / this.sampleTextScale),
                        this.color.getColor().getRGB() & 0xFFFFFF | (int) (this.opacity * 255) << 24);
                GlStateManagerWrapper.scale(1 / this.sampleTextScale);
            }
            super.hookRender(mouseX, mouseY, partialTicks);
        } else {
            // Quickplay is disabled, draw error message
            this.drawCenteredString(Quickplay.INSTANCE.elementController
                            .translate("quickplay.disabled", Quickplay.INSTANCE.disabledReason),
                    this.getWidth() / 2, this.getHeight() / 2, 0xffffff);
        }

        GlStateManagerWrapper.disableBlend();
        GlStateManagerWrapper.popMatrix();
    }

    @Override
    public void hookInit() {
        super.hookInit();

        this.nameTextScale = 1.0;
        this.nameTextY = (int) (this.getHeight() * 0.2);
        this.sampleTextScale = 1.5;
        this.sampleTextY = (int) (this.nameTextY + this.getFontHeight() * this.nameTextScale + 4);

        int elementWidth = (this.getWidth() > 200 + QuickplayGuiEditColor.elementMargins * 2 ? 200 :
                this.getWidth() - QuickplayGuiEditColor.elementMargins * 2);

        int nextComponentId = 0;

        ColorGuiResponder colorGuiResponder = new ColorGuiResponder();
        ColorFormatHelper formatHelper = new ColorFormatHelper();

        final int sampleTextBottom = (int) (this.sampleTextY + this.getFontHeight() * this.sampleTextScale);

        this.componentList.add(new QuickplayGuiSlider(colorGuiResponder, "RED", nextComponentId,
                this.getWidth() / 2 - elementWidth / 2, sampleTextBottom + QuickplayGuiEditColor.elementMargins +
                (QuickplayGuiEditColor.elementHeight + QuickplayGuiEditColor.elementMargins) * nextComponentId,
                elementWidth, QuickplayGuiEditColor.elementHeight,
                Quickplay.INSTANCE.elementController.translate("quickplay.config.color.gui.red"),
                0, 255, this.color.getColor().getRed(), formatHelper, true));

        nextComponentId++;
        this.componentList.add(new QuickplayGuiSlider(colorGuiResponder, "GREEN", nextComponentId,
                this.getWidth() / 2 - elementWidth / 2, sampleTextBottom + QuickplayGuiEditColor.elementMargins +
                (QuickplayGuiEditColor.elementHeight + QuickplayGuiEditColor.elementMargins) * nextComponentId,
                elementWidth, QuickplayGuiEditColor.elementHeight,
                Quickplay.INSTANCE.elementController.translate("quickplay.config.color.gui.green"),
                0, 255, this.color.getColor().getGreen(), formatHelper, true));

        nextComponentId++;
        this.componentList.add(new QuickplayGuiSlider(colorGuiResponder, "BLUE", nextComponentId,
                this.getWidth() / 2 - elementWidth / 2, sampleTextBottom + QuickplayGuiEditColor.elementMargins +
                (QuickplayGuiEditColor.elementHeight + QuickplayGuiEditColor.elementMargins) * nextComponentId,
                elementWidth, QuickplayGuiEditColor.elementHeight,
                Quickplay.INSTANCE.elementController.translate("quickplay.config.color.gui.blue"),
                0, 255, this.color.getColor().getBlue(), formatHelper, true));

        nextComponentId++;
        this.componentList.add(new QuickplayGuiSlider(colorGuiResponder, "CHROMA", nextComponentId,
                this.getWidth() / 2 - elementWidth / 2, sampleTextBottom + QuickplayGuiEditColor.elementMargins +
                (QuickplayGuiEditColor.elementHeight + QuickplayGuiEditColor.elementMargins) * nextComponentId,
                elementWidth, QuickplayGuiEditColor.elementHeight,
                Quickplay.INSTANCE.elementController.translate("quickplay.config.color.gui.chromaspeed"),
                0, QuickplayGuiEditColor.chromaMaxSpeed, this.color.getChromaSpeed(), formatHelper, true));

        nextComponentId++;
        this.componentList.add(new QuickplayGuiButton("EXIT", nextComponentId, this.getWidth() / 2 - elementWidth / 2,
                sampleTextBottom + QuickplayGuiEditColor.elementMargins +
                        (QuickplayGuiEditColor.elementHeight + QuickplayGuiEditColor.elementMargins) * nextComponentId,
                elementWidth,
                QuickplayGuiEditColor.elementHeight,
                Quickplay.INSTANCE.elementController
                        .translate("quickplay.gui." + (this.previousGui == null ? "close" : "back")), true));
    }

    @Override
    public void mouseScrolled(int distance) {
        // This GUI does not have scrolling capability
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        if(component.origin.equals("EXIT")) {
            Quickplay.INSTANCE.minecraft.openGui(previousGui);
        }
    }

    @Override
    public boolean hookMouseReleased(int mouseX, int mouseY, int state) {
        super.hookMouseReleased(mouseX, mouseY, state);
        try {
            this.config.save();
        } catch (IOException e) {
            System.out.println("Failed to save color " + this.colorName + ".");
            Quickplay.INSTANCE.minecraft.sendLocalMessage(new Message(new QuickplayChatComponentTranslation("quickplay.config.saveError")
                    .setStyle(new ChatStyleWrapper().apply(Formatting.RED))));
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
        return true;
    }

    /**
     * A slider formatter for formatting the sliders display text
     */
    public class ColorFormatHelper implements QuickplayGuiSlider.FormatHelper {
        @Override
        public String getText(int id, String name, float value) {
            // 0-2 = R, G, and B
            if (id == 3) { // 3 = Chroma
                String speedLang;
                if (value <= 0) {
                    speedLang = "off";
                } else if (value < 0.008) {
                    speedLang = "slow";
                } else if (value < 0.02) {
                    speedLang = "medium";
                } else if (value < 0.035) {
                    speedLang = "fast";
                } else {
                    speedLang = "insane";
                }
                return name + ": " + Quickplay.INSTANCE.elementController.translate("quickplay.config.color.gui.chromaspeed." + speedLang);
            }
            return name + ": " + ((Number) value).intValue();
        }
    }

    /**
     * A GUI responder used for saving the values of sliders when the value changes
     */
    public class ColorGuiResponder implements GuiPageButtonList.GuiResponder {
        /**
         * Fired every tick for a boolean-based GUI element change
         * @param p_175321_1_ ID of the element
         * @param p_175321_2_ Value
         */
        @Override
        public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {

        }

        /**
         * Fired every tick for a float-based GUI element change
         * @param id ID of the element
         * @param value Value
         */
        @Override
        public void onTick(int id, float value) {
            if ("CHROMA".equals(componentList.get(id).origin)) {
                color.setChromaSpeed(value);
            } else {
                color.setColor(new Color(((int) ((QuickplayGuiSlider) componentList.get(0)).getValue()),
                        (int) ((QuickplayGuiSlider) componentList.get(1)).getValue(),
                        (int) ((QuickplayGuiSlider) componentList.get(2)).getValue()));
            }
        }

        /**
         * Fired every tick for a text-based GUI element change
         * @param p_175319_1_ ID of the element
         * @param p_175319_2_ Value
         */
        @Override
        public void func_175319_a(int p_175319_1_, String p_175319_2_) {

        }
    }
}
