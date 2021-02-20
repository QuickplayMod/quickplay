package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiSlider;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.ParametersAreNonnullByDefault;
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        /*
         * Draw background
         */

        drawDefaultBackground();

        // Draw text
        if(opacity > 0) {
            GlStateManager.scale(nameTextScale, nameTextScale, nameTextScale);
            drawCenteredString(mc.fontRenderer, colorName, (int) (width / 2 / nameTextScale),
                    (int) (nameTextY / nameTextScale), 0xFFFFFF);
            GlStateManager.scale(1 / nameTextScale, 1 / nameTextScale, 1 / nameTextScale);

            GlStateManager.scale(sampleTextScale, sampleTextScale, sampleTextScale);
            drawCenteredString(mc.fontRenderer, I18n.format("quickplay.config.color.gui.sampletext"),
                    (int) (width / 2 / sampleTextScale), (int) (sampleTextY / sampleTextScale),
                    color.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
            GlStateManager.scale(1 / sampleTextScale, 1 / sampleTextScale, 1 / sampleTextScale);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

    }

    @Override
    public void initGui() {
        super.initGui();

        nameTextScale = 1.0;
        nameTextY = (int) (height * 0.2);
        sampleTextScale = 1.5;
        sampleTextY = (int) (nameTextY + fontRenderer.FONT_HEIGHT * nameTextScale + 4);

        int elementWidth = (width > 200 + elementMargins * 2 ? 200 : width - elementMargins * 2);

        int nextComponentId = 0;

        ColorGuiResponder colorGuiResponder = new ColorGuiResponder();
        ColorFormatHelper formatHelper = new ColorFormatHelper();

        final int sampleTextBottom = (int) (sampleTextY + mc.fontRenderer.FONT_HEIGHT * sampleTextScale);

        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "RED", nextComponentId,
                width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins)
                * nextComponentId, elementWidth, elementHeight, I18n.format("quickplay.config.color.gui.red"),
                0, 255, color.getColor().getRed(), formatHelper, true));
        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "GREEN", nextComponentId,
                width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins)
                * nextComponentId, elementWidth, elementHeight, I18n.format("quickplay.config.color.gui.green"),
                0, 255, color.getColor().getGreen(), formatHelper, true));

        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "BLUE", nextComponentId,
                width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins)
                * nextComponentId, elementWidth, elementHeight, I18n.format("quickplay.config.color.gui.blue"),
                0, 255, color.getColor().getBlue(), formatHelper, true));

        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "CHROMA", nextComponentId,
                width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins)
                * nextComponentId, elementWidth, elementHeight, I18n.format("quickplay.config.color.gui.chromaspeed"),
                0, chromaMaxSpeed, color.getChromaSpeed(), formatHelper, true));

        nextComponentId++;
        componentList.add(new QuickplayGuiButton("EXIT", nextComponentId, width / 2 - elementWidth / 2,
                sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth,
                elementHeight, I18n.format("quickplay.gui." + (previousGui == null ? "close" : "back")), true));
    }

    @Override
    public void mouseScrolled(int distance) {
        // This GUI does not have scrolling capability
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        if(component.origin.equals("EXIT")) {
            mc.displayGuiScreen(previousGui);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        try {
            config.save();
        } catch (IOException e) {
            System.out.println("Failed to save color " + colorName + ".");
            Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.config.saveerror")
                    .setStyle(new Style().setColor(TextFormatting.RED))));
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
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
                return name + ": " + I18n.format("quickplay.config.color.gui.chromaspeed." + speedLang);
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
        public void setEntryValue(int p_175321_1_, boolean p_175321_2_) {

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
        @ParametersAreNonnullByDefault
        public void setEntryValue(int p_175319_1_, String p_175319_2_) {

        }
    }
}
