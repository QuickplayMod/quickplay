package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.QuickplayGuiSlider;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class QuickplayGuiEditColor extends QuickplayGui {

    public QuickplayColor color;
    public String colorName;
    public AConfiguration config;
    public QuickplayGui previousGui;

    public int sampleTextY;
    public double sampleTextScale;
    public int nameTextY;
    public double nameTextScale;

    public static int elementMargins = 4;
    public static int elementHeight = 20;
    public static float chromaMaxSpeed = 0.05f;

    public QuickplayGuiEditColor(QuickplayColor color, String colorName, AConfiguration config) {
        this(color, colorName, config, null);
    }

    public QuickplayGuiEditColor(QuickplayColor color, String colorName, AConfiguration config, QuickplayGui previousGui) {
        this.color = color;
        this.colorName = colorName;
        this.config = config;
        this.previousGui = previousGui;
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw background
         */

        drawDefaultBackground();

        GL11.glScaled(nameTextScale, nameTextScale, nameTextScale);
        drawCenteredString(mc.fontRendererObj, colorName, (int) (width / 2 / nameTextScale), (int) (nameTextY / nameTextScale), 0xFFFFFF);
        GL11.glScaled(1 / nameTextScale, 1 / nameTextScale, 1 / nameTextScale);
        GL11.glScaled(sampleTextScale, sampleTextScale, sampleTextScale);
        drawCenteredString(mc.fontRendererObj, new ChatComponentTranslation("quickplay.config.color.gui.sampletext").getUnformattedText(), (int) (width / 2 / sampleTextScale), (int) (sampleTextY / sampleTextScale), color.getColor().getRGB() & 0xFFFFFF | (int) (opacity * 255) << 24);
        GL11.glScaled(1 / sampleTextScale, 1 / sampleTextScale, 1 / sampleTextScale);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }

    @Override
    public void initGui() {
        super.initGui();

        nameTextScale = 1.0;
        nameTextY = (int) (height * 0.2);
        sampleTextScale = 1.5;
        sampleTextY = (int) (nameTextY + fontRendererObj.FONT_HEIGHT * nameTextScale + 4);

        int elementWidth = (width > 200 + elementMargins * 2 ? 200 : width - elementMargins * 2);

        int nextComponentId = 0;

        ColorGuiResponder colorGuiResponder = new ColorGuiResponder();
        ColorFormatHelper formatHelper = new ColorFormatHelper();

        final int sampleTextBottom = (int) (sampleTextY + mc.fontRendererObj.FONT_HEIGHT * sampleTextScale);
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "RED", nextComponentId, width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth, elementHeight, new ChatComponentTranslation("quickplay.config.color.gui.red").getUnformattedText(), 0, 255, color.getColor().getRed(), formatHelper));
        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "GREEN", nextComponentId, width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth, elementHeight, new ChatComponentTranslation("quickplay.config.color.gui.green").getUnformattedText(), 0, 255, color.getColor().getGreen(), formatHelper));
        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "BLUE", nextComponentId, width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth, elementHeight, new ChatComponentTranslation("quickplay.config.color.gui.blue").getUnformattedText(), 0, 255, color.getColor().getBlue(), formatHelper));
        nextComponentId++;
        componentList.add(new QuickplayGuiSlider(colorGuiResponder, "CHROMA", nextComponentId, width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth, elementHeight, new ChatComponentTranslation("quickplay.config.color.gui.chromaspeed").getUnformattedText(), 0, chromaMaxSpeed, color.getChromaSpeed(), formatHelper));
        nextComponentId++;
        componentList.add(new QuickplayGuiButton("EXIT", nextComponentId, width / 2 - elementWidth / 2, sampleTextBottom + elementMargins + (elementHeight + elementMargins) * nextComponentId, elementWidth, elementHeight, new ChatComponentTranslation("quickplay.gui." + (previousGui == null ? "close" : "back")).getUnformattedText()));
    }

    @Override
    public void mouseScrolled(int distance) {

    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        if(component.origin.equals("EXIT"))
            mc.displayGuiScreen(previousGui);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        try {
            config.save();
        } catch (IOException e) {
            System.out.println("Failed to save color " + colorName + ".");
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.config.saveerror").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }

    public class ColorFormatHelper implements QuickplayGuiSlider.FormatHelper {
        @Override
        public String getText(int id, String name, float value) {
            switch(id) {
                default: // 0-2 = R, G, and B
                    return name + ": " + ((Number) value).intValue();
                case 3: // 3 = Chroma
                    String speedLang;
                    if(value <= 0)
                        speedLang = "off";
                    else if(value < 0.008)
                        speedLang = "slow";
                    else if(value < 0.02)
                        speedLang = "medium";
                    else if(value < 0.035)
                        speedLang = "fast";
                    else
                        speedLang = "insane";
                    return name + ": " + new ChatComponentTranslation("quickplay.config.color.gui.chromaspeed." + speedLang).getUnformattedText();
            }
        }
    }

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
            switch((String) componentList.get(id).origin) {
                default:
                    color.setColor(new Color(((int) ((QuickplayGuiSlider) componentList.get(0)).getValue()), (int) ((QuickplayGuiSlider) componentList.get(1)).getValue(), (int) ((QuickplayGuiSlider) componentList.get(2)).getValue()));
                    break;
                case "CHROMA":
                    color.setChromaSpeed(value);
                    break;
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
