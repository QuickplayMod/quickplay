package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.*;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.config.GuiOption;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
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

        drawDefaultBackground();

        /*
         * Draw the header text
         */

        // Scale up to header size
        GL11.glScaled(headerScale, headerScale, headerScale);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("quickplay.config.gui.title").getUnformattedText(), (int) (width / 2 / headerScale), (int) (height * 0.05 / headerScale),
                       // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        // Scale back down
        GL11.glScaled( 1 / headerScale, 1 / headerScale, 1 / headerScale);

        // Scale up to subheader size
        GL11.glScaled(subheaderScale, subheaderScale, subheaderScale);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("quickplay.config.gui.version").getUnformattedText() + " " + Reference.VERSION, (int) (width / 2 / subheaderScale),
                    subheaderY,
                       // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                (Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
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
            int buttonsAboveFadeLine = componentList.stream().filter((button) -> button.y < scrollFadeLine).collect(Collectors.toList()).size();
            int buttonsBelowScreen = componentList.stream().filter((button) -> button.y < height).collect(Collectors.toList()).size();
            drawRect(
                    (int) ((width * (1 - boxMargins)) - scrollbarWidth) - ConfigElement.ELEMENT_MARGINS,
                    (int) ((buttonsAboveFadeLine / (double) componentList.size() * (double) height + (double) topOfBox) + (double) ConfigElement.ELEMENT_MARGINS),
                    (int) (width * (1 - boxMargins)) - ConfigElement.ELEMENT_MARGINS,
                    (int) ((buttonsBelowScreen / (double) componentList.size() * (double) height) - (double) ConfigElement.ELEMENT_MARGINS),
                    (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
        }


        /*
         * Draw buttons & labels
         */
        for (QuickplayGuiComponent component : componentList)
        {
            double scrollOpacity = (component.y > scrollFadeLine ? 1 : component.y + ConfigElement.ELEMENT_HEIGHT < scrollFadeLine ? 0 : (ConfigElement.ELEMENT_HEIGHT - ((double) scrollFadeLine - (double) component.y)) / (double) ConfigElement.ELEMENT_HEIGHT);
            component.opacity = scrollOpacity;
            if(component.y + ConfigElement.ELEMENT_HEIGHT > scrollFadeLine) component.draw(this.mc, mouseX, mouseY, opacity * (float) scrollOpacity);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void initGui() {
        componentList.clear();
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

        /*
         * Sort elements
         */
        configElements.sort(Comparator.comparing(o -> o.optionInfo.category()));

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

        String previousCategory = null;

        for(ConfigElement element : configElements) {
            if(previousCategory == null || !previousCategory.equals(element.optionInfo.category())) {
                componentList.add(new QuickplayGuiHeader(null, nextButtonId, width / 2, getY(nextButtonId) + ConfigElement.ELEMENT_HEIGHT - ConfigElement.ELEMENT_MARGINS - mc.fontRendererObj.FONT_HEIGHT, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.category()));
                nextButtonId++;
            }
            previousCategory = element.optionInfo.category();

            int buttonX = width / 2 - (ConfigElement.ELEMENT_MARGINS + buttonWidth) / 2;
            int buttonY = getY(nextButtonId);

            // Figure out what button type needs to be rendered & give it the appropriate text
            if(element.element instanceof Boolean)
                componentList.add(new QuickplayGuiButton(element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name() + ": " + new ChatComponentTranslation((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false").getUnformattedText()));
            else if(element.element instanceof QuickplayColor || element.element instanceof Runnable)
                componentList.add(new QuickplayGuiButton(element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name()));
            else if(element.element instanceof Double)
                componentList.add(new QuickplayGuiSlider(guiResponder, element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, element.optionInfo.name(), element.optionInfo.minValue(), element.optionInfo.maxValue(), ((Number) element.element).floatValue(), formatHelper));

            nextButtonId++;
        }
    }

    public int getY(int id) {
        return (scrollFadeLine + ConfigElement.ELEMENT_MARGINS + ((ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS) * (id)));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        System.out.println("Key typed");
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        System.out.println("Action performed");
        // Only do something if the component is visible
        if(((QuickplayGuiButton) component).opacity > 0) {
            super.componentClicked(component);

            final ConfigElement element = (ConfigElement) component.origin;
            if(element != null) {
                if(element.element instanceof Boolean) {
                    element.element = !(boolean) element.element;
                    component.displayString = element.optionInfo.name() + ": " + new ChatComponentTranslation((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false").getUnformattedText();
                } else if(element.element instanceof Runnable) {
                    mc.displayGuiScreen(null);
                    ((Runnable) element.element).run();
                } else if(element.element instanceof QuickplayColor) {
                    mc.displayGuiScreen(new EditColor((QuickplayColor) element.element, element.optionInfo.name(), config));
                }

                save(element);
            }
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
                if((distance < 0 && componentList.get(componentList.size() - 1).y > height - ConfigElement.ELEMENT_HEIGHT - bottomScrollMargins) ||
                   // OR if scrolling up & the top element is currently off of the screen (above the fade line)
                   (distance > 0 && componentList.get(0).y < scrollFadeLine + ConfigElement.ELEMENT_MARGINS)) {
                        for (QuickplayGuiComponent component : componentList) {
                            component.move(distance < 0 ? -1 : 1);
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
            ConfigElement element = (ConfigElement) componentList.get(id).origin;
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
            String pattern;
            if(componentList.size() >= id + 1 && componentList.get(id).origin != null)
                pattern = ((ConfigElement) componentList.get(id).origin).optionInfo.decimalFormat();
            else
                pattern = "0.00";

            final DecimalFormat format = new DecimalFormat(pattern);
            return name + ": " + format.format(value);
        }
    }
}
