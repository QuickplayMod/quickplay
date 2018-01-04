package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.QuickplayGuiSlider;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.config.GuiOption;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditConfiguration extends QuickplayGui {
    public AConfiguration config;
    public List<ConfigElement> configElements = new ArrayList<>();

    public EditConfiguration(AConfiguration config) {
        this.config = config;
    }

    public double headerScale;
    public double subheaderScale;
    public int subheaderY;
    public double boxMargins;
    public int topOfBox;
    public int boxWidth;
    public int boxHeight;
    public int elementSize;
    public int scrollFadeLine;
    public int scrollbarWidth = 3;
    public boolean scrollbarDrawn;
    public int bottomScrollMargins;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Blend is enabled for the GUI fadein
        // Fade in opacity has to be applied individually to each component that you want to fade in
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw background
         */

        // Prepend opacity to 24-bit color
        drawRect(0, 0, width, height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));
        // drawRect disables blend (Grr!)
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw the header text
         */

        // Scale up to header size
        GL11.glScaled(headerScale, headerScale, headerScale);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("quickplay.config.gui.title").getUnformattedText(), (int) (width / 2 / headerScale), (int) (height * 0.05 / headerScale),
                       // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                (Quickplay.INSTANCE.settings.primaryColor.getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        // Scale back down
        GL11.glScaled( 1 / headerScale, 1 / headerScale, 1 / headerScale);

        // Scale up to subheader size
        GL11.glScaled(subheaderScale, subheaderScale, subheaderScale);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("quickplay.config.gui.version").getUnformattedText() + " " + Reference.VERSION, (int) (width / 2 / subheaderScale),
                    subheaderY,
                       // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                (Quickplay.INSTANCE.settings.secondaryColor.getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        // Scale back down
        GL11.glScaled(1 / subheaderScale, 1 / subheaderScale, 1 / subheaderScale);

        /*
         * Draw options list background
         */

        drawRect((int) (width * boxMargins), topOfBox, (int) (width * (1 - boxMargins)), height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));

        /*
         * Draw scroll bar
         * TODO This is pretty meh. Should be made better probs.
         */
        if(scrollbarDrawn) {
            int buttonsAboveFadeLine = buttonList.stream().filter((button) -> button.yPosition < scrollFadeLine).collect(Collectors.toList()).size();
            int buttonsBelowScreen = buttonList.stream().filter((button) -> button.yPosition < height).collect(Collectors.toList()).size();
            drawRect(
                    (int) ((width * (1 - boxMargins)) - scrollbarWidth) - ConfigElement.ELEMENT_MARGINS,
                    (int) ((buttonsAboveFadeLine / (double) buttonList.size() * (double) height + (double) topOfBox) + (double) ConfigElement.ELEMENT_MARGINS),
                    (int) (width * (1 - boxMargins)) - ConfigElement.ELEMENT_MARGINS,
                    (int) ((buttonsBelowScreen / (double) buttonList.size() * (double) height) - (double) ConfigElement.ELEMENT_MARGINS),
                    (Quickplay.INSTANCE.settings.primaryColor.getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        }


        /*
         * Draw buttons & labels
         */
        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            final QuickplayGuiButton button = (QuickplayGuiButton) this.buttonList.get(i);
            double scrollOpacity = (button.yPosition > scrollFadeLine ? 1 : button.yPosition + ConfigElement.ELEMENT_HEIGHT < scrollFadeLine ? 0 : (ConfigElement.ELEMENT_HEIGHT - ((double) scrollFadeLine - (double) button.yPosition)) / (double) ConfigElement.ELEMENT_HEIGHT);
            button.lastOpacity = scrollOpacity;
            if(button.yPosition + ConfigElement.ELEMENT_HEIGHT > scrollFadeLine) button.drawButton(this.mc, mouseX, mouseY, opacity * (float) scrollOpacity);
        }

        for (int j = 0; j < this.labelList.size(); ++j)
        {
            ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        configElements.clear();
        super.initGui();

        /*
         * Calculate various sizes and positions
         */

        // Header size is responsive to screen size
        headerScale = height > 400 ? 2 : 1.5;
        subheaderScale = height > 400 ? 1.3 : 1;

        subheaderY = // Subheader should be 3 pixels below main header
                (int) (height * 0.05 / subheaderScale) + (int) (fontRendererObj.FONT_HEIGHT * headerScale) + (int) (3 / headerScale);

        // Padding on the sides of the list (responsive)
        boxMargins = width < 500 ? 0.1 : 0.2;
        // +20 to the top because for some reason subheaderY + subheader height isn't actually the bottom of the subheader... fix
        topOfBox = (int) (subheaderY + fontRendererObj.FONT_HEIGHT * subheaderScale + 20);

        boxWidth = (int) (width * (1 - (boxMargins * 2)));
        boxHeight = height - topOfBox;

        elementSize = (ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS);

        // If small height, sacrifice pretty fade for more button space
        System.out.println(height);
        scrollFadeLine = topOfBox + (height > 250 ? ConfigElement.ELEMENT_HEIGHT : 0);
        bottomScrollMargins = 10;

        /*
         * Get the config elements that can be changed
         */
        Field[] fields = config.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            GuiOption guiOptionDisplay = field.getAnnotation(GuiOption.class);
            if(guiOptionDisplay != null) {
                try {
                    configElements.add(new ConfigElement(field.get(config), guiOptionDisplay, field.getName()));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        // If at least the last button is going to be off screen, scroll bar should be drawn
        scrollbarDrawn = scrollFadeLine + ConfigElement.ELEMENT_MARGINS + ((ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS) * (configElements.size())) + bottomScrollMargins > height;

        /*
         * Create the necessary buttons
         */

        int nextButtonId = 0;
        // Get the width of each button
        int buttonWidth = 200;
        if(boxWidth < 200 + ConfigElement.ELEMENT_MARGINS * 2)
            // If scroll bar is being drawn, buttons should be moved over a lil bit to give it room
            buttonWidth = boxWidth - ConfigElement.ELEMENT_MARGINS * 2 - (scrollbarDrawn ? scrollbarWidth + ConfigElement.ELEMENT_MARGINS : 0);

        // These objects help format & handle changes to sliders, text boxes, and boolean boxes
        ConfigGuiResponder guiResponder = new ConfigGuiResponder();
        SliderFormatHelper formatHelper = new SliderFormatHelper();

        for(ConfigElement element : configElements) {
            int buttonX = width / 2 - (ConfigElement.ELEMENT_MARGINS + buttonWidth) / 2;
            int buttonY = scrollFadeLine + ConfigElement.ELEMENT_MARGINS + ((ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS) * (nextButtonId));

            // Figure out what button type needs to be rendered & give it the appropriate text
            if(element.element instanceof Boolean)
                buttonList.add(new QuickplayGuiButton(nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name() + ": " + new ChatComponentTranslation((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false").getUnformattedText()));
            else if(element.element instanceof Color || element.element instanceof Runnable)
                buttonList.add(new QuickplayGuiButton(nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name()));
            else if(element.element instanceof Double)
                buttonList.add(new QuickplayGuiSlider(guiResponder, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name(), element.optionInfo.minValue(), element.optionInfo.maxValue(), ((Number) element.element).floatValue(), formatHelper));

            nextButtonId++;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        System.out.println("Key typed");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        System.out.println("Action performed");
        // Only do something if the button is visible
        if(((QuickplayGuiButton) button).lastOpacity > 0) {
            final ConfigElement element = configElements.get(button.id);
            if(element.element instanceof Boolean) {
                element.element = !(boolean) element.element;
                button.displayString = element.optionInfo.name() + ": " + new ChatComponentTranslation((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false").getUnformattedText();
            } else if(element.element instanceof Runnable) {
                Minecraft.getMinecraft().displayGuiScreen(null);
                ((Runnable) element.element).run();
            }

            save(element);
        }
    }

    public void save(ConfigElement element) {
        // Try to apply the changed value to the config & then save the config
        try {
            config.getClass().getField(element.configFieldName).set(config, element.element);
            config.save();
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Failed to save option " + element.configFieldName + ".");
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.config.saveerror")));
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        System.out.println("Mouse clicked");
        // lastMouseY is used for dragging scrolling
        lastMouseY = mouseY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        System.out.println("Mouse released");
    }

    int lastMouseY = 0;
    int mouseYMovement = 0;
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        System.out.println("Mouse dragged");
        mouseYMovement = lastMouseY - mouseY;
        lastMouseY = mouseY;
        System.out.println(mouseYMovement);
        // Scroll should be the same direction the mouse is moving
        if(mouseYMovement != 0) scroll(mouseYMovement * -1);
    }

    @Override
    public void mouseScrolled(int distance) {
        System.out.println("Mouse scrolled");

        // Divide the distance by 10 as "120" px is way too much
        final int splitDistance = distance / 10;
        scroll(splitDistance);
    }

    public void scroll(int distance) {
        // Scroll is animated, one pixel per 5ms
        Quickplay.INSTANCE.threadPool.submit(() -> {
            for (int i = 0; i < Math.abs(distance); i++) {

                // Only allow scrolling if there is an element off screen
                // If scrolling down & the last element is at all off the screen (plus the additional margins for aesthetic purposes)
                if((distance < 0 && buttonList.get(buttonList.size() - 1).yPosition > height - ConfigElement.ELEMENT_HEIGHT - bottomScrollMargins) ||
                   // OR if scrolling up & the top element is currently off of the screen (above the fade line)
                   (distance > 0 && buttonList.get(0).yPosition < scrollFadeLine + ConfigElement.ELEMENT_MARGINS)) {
                        for (GuiButton button : buttonList) {
                            ((QuickplayGuiButton) button).move(distance < 0 ? -1 : 1);
                        }
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                } else {
                    // Already reached the bottom/top, so stop trying to scroll
                    break;
                }
            }
        });
    }

    // ------

    public class ConfigGuiResponder implements GuiPageButtonList.GuiResponder {

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
            ConfigElement element = configElements.get(id);
            element.element = ((Number) value).doubleValue();
            save(element);
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

    public class SliderFormatHelper implements QuickplayGuiSlider.FormatHelper {
        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + value;
        }
    }
}
